package microdata.absensi.ui.izin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.data.model.AbsenRequest
import microdata.absensi.data.remote.RetrofitClient
import microdata.absensi.utils.ImageUtils
import microdata.absensi.utils.UtilsSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IzinFragment : Fragment() {

    private lateinit var ivPreview: ImageView
    private lateinit var tvStatus: TextView
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnIzin: MaterialButton

    private var photoUri: Uri? = null
    private var sudahAbsenHariIni = false

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let { uri ->
                    Log.d("Izin", "photoUri=$uri")
                    val bitmap = ImageUtils.loadSampledBitmap(requireContext(), uri)
                    Log.d("Izin", "bitmap=${bitmap?.width}x${bitmap?.height}")
                    if (bitmap != null) {
                        ivPreview.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat foto", Toast.LENGTH_SHORT).show()
                    }
                } ?: Log.d("Izin", "photoUri null")
            } else {
                Log.d("Izin", "resultCode=${result.resultCode}")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_izin, container, false)

        ivPreview = view.findViewById(R.id.iv_preview)
        tvStatus = view.findViewById(R.id.tv_status)
        etKeterangan = view.findViewById(R.id.et_keterangan)
        btnIzin = view.findViewById(R.id.btn_izin)

        view.findViewById<MaterialButton>(R.id.btn_foto).setOnClickListener {
            openCamera()
        }

        btnIzin.setOnClickListener {
            submitIzin()
        }

        checkSudahAbsen()

        return view
    }

    private fun openCamera() {
        val photoFile = createPhotoFile() ?: return
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        photoUri = uri

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "Tidak ada aplikasi kamera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPhotoFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            File.createTempFile("izin_$timeStamp", ".jpg", requireContext().cacheDir)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal buat file foto", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun checkSudahAbsen() {
        val session = UtilsSession(requireContext())
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (session.getAbsenDate("IZIN") == today) {
            sudahAbsenHariIni = true
            btnIzin.isEnabled = false
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "Anda sudah izin hari ini"
        }
    }

    private fun submitIzin() {
        val uri = photoUri
        val keterangan = etKeterangan.text?.toString()?.trim() ?: ""

        if (uri == null) {
            Toast.makeText(requireContext(), "Ambil foto bukti dulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (keterangan.isEmpty()) {
            Toast.makeText(requireContext(), "Isi keterangan / alasan izin dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val token = UtilsSession(requireContext()).getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Session tidak valid, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val fileBukti = ImageUtils.encodeToBase64(requireContext(), uri)
        if (fileBukti.isEmpty()) {
            Toast.makeText(requireContext(), "Gagal proses foto", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val request = AbsenRequest(
            kodeJenis = "IZIN",
            koordinat = "",
            keterangan = keterangan,
            fileBukti = fileBukti
        )

        btnIzin.isEnabled = false
        btnIzin.text = "Mengirim..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.absen("Bearer $token", deviceId, request)
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Izin terkirim",
                        Toast.LENGTH_SHORT
                    ).show()
                    UtilsSession(requireContext()).saveAbsenDate("IZIN")
                    sudahAbsenHariIni = true
                    btnIzin.isEnabled = false
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = "Anda sudah izin hari ini"
                    resetForm()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error koneksi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                btnIzin.isEnabled = true
                btnIzin.text = "Kirim Izin"
            }
        }
    }

    private fun resetForm() {
        photoUri = null
        ivPreview.setImageURI(null)
        etKeterangan.text?.clear()
    }
}