package microdata.absensi.ui.absen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.data.model.AbsenRequest
import microdata.absensi.data.remote.RetrofitClient
import microdata.absensi.utils.UtilsSession
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AbsenMasukFragment : Fragment() {

    private lateinit var ivPreview: ImageView
    private lateinit var tvKoordinat: TextView
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnAbsen: MaterialButton
    private lateinit var mapView: MapView

    private var photoUri: Uri? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private var marker: Marker? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.all { it }) {
                getLocation()
            } else {
                tvKoordinat.text = "Koordinat: izin lokasi ditolak"
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let { uri ->
                    ivPreview.setImageURI(uri)
                    btnAbsen.isEnabled = true
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_absen_masuk, container, false)

        ivPreview = view.findViewById(R.id.iv_preview)
        tvKoordinat = view.findViewById(R.id.tv_koordinat)
        etKeterangan = view.findViewById(R.id.et_keterangan)
        btnAbsen = view.findViewById(R.id.btn_absen)
        mapView = view.findViewById(R.id.map)

        view.findViewById<TextView>(R.id.tv_tanggal).text =
            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())

        setupMap()

        view.findViewById<MaterialButton>(R.id.btn_foto).setOnClickListener {
            openCamera()
        }

        view.findViewById<MaterialButton>(R.id.btn_muat_lokasi).setOnClickListener {
            getLocation()
        }

        btnAbsen.setOnClickListener {
            submitAbsen()
        }

        if (hasLocationPermission()) {
            getLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        return view
    }

    private fun setupMap() {
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(17.0)
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) {
            mapView.onPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::mapView.isInitialized) {
            mapView.onDetach()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            lat = location.latitude
            lng = location.longitude
            tvKoordinat.text = String.format(
                Locale.getDefault(),
                "Koordinat: %.6f, %.6f",
                location.latitude,
                location.longitude
            )
            updateMap(location)
        } else {
            tvKoordinat.text = "Koordinat: belum didapat, pastikan GPS aktif"
        }
    }

    private fun updateMap(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        mapView.controller.animateTo(geoPoint)

        marker?.let { mapView.overlays.remove(it) }
        marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Lokasi Anda"
            mapView.overlays.add(this)
        }
        mapView.invalidate()
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
            File.createTempFile("bukti_$timeStamp", ".jpg", requireContext().cacheDir)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal buat file foto", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun photoToBase64(uri: Uri): String {
        val file = File(uri.path ?: "")
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var sampleSize = 1
        val maxDim = 1024
        while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return ""

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        bitmap.recycle()

        val encoded = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$encoded"
    }

    private fun submitAbsen() {
        val uri = photoUri
        val currentLat = lat
        val currentLng = lng

        if (uri == null) {
            Toast.makeText(requireContext(), "Ambil foto bukti dulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentLat == null || currentLng == null) {
            Toast.makeText(requireContext(), "Koordinat belum didapat", Toast.LENGTH_SHORT).show()
            return
        }

        val token = UtilsSession(requireContext()).getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Session tidak valid, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val fileBukti = photoToBase64(uri)
        if (fileBukti.isEmpty()) {
            Toast.makeText(requireContext(), "Gagal proses foto", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val request = AbsenRequest(
            kodeJenis = "MASUK",
            koordinat = String.format(Locale.getDefault(), "%.6f, %.6f", currentLat, currentLng),
            keterangan = etKeterangan.text?.toString()?.trim() ?: "",
            fileBukti = fileBukti
        )

        btnAbsen.isEnabled = false
        btnAbsen.text = "Mengirim..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.absen("Bearer $token", deviceId, request)
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Absen berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                    resetForm()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Absen gagal: ${response.code()}",
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
                btnAbsen.isEnabled = true
                btnAbsen.text = "Absen Masuk"
            }
        }
    }

    private fun resetForm() {
        photoUri = null
        ivPreview.setImageURI(null)
        etKeterangan.text?.clear()
        btnAbsen.isEnabled = false
    }
}