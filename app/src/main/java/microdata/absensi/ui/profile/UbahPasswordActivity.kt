package microdata.absensi.ui.profile

import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.data.model.UbahPasswordRequest
import microdata.absensi.data.remote.RetrofitClient
import microdata.absensi.utils.UtilsSession

class UbahPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubah_password)

        val etPasswordLama = findViewById<TextInputEditText>(R.id.etPasswordLama)
        val etPasswordBaru = findViewById<TextInputEditText>(R.id.etPasswordBaru)
        val etKonfirmasi = findViewById<TextInputEditText>(R.id.etKonfirmasiPassword)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanPassword)

        btnSimpan.setOnClickListener {
            val passLama = etPasswordLama.text.toString().trim()
            val passBaru = etPasswordBaru.text.toString().trim()
            val konfirmasi = etKonfirmasi.text.toString().trim()

            // 1. Validasi Input
            if (passLama.isEmpty() || passBaru.isEmpty() || konfirmasi.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passBaru != konfirmasi) {
                Toast.makeText(this, "Password baru dan konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val session = UtilsSession(this)
            val token = session.getToken() ?: ""
            val bearerToken = "Bearer $token" // Format standar token
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            ubahPassword(bearerToken, deviceId, passLama, passBaru)
        }
    }

    private fun ubahPassword(token: String, deviceId: String, passLama: String, passBaru: String) {
        lifecycleScope.launch {
            try {
                val request = UbahPasswordRequest(passLama, passBaru)
                val response = RetrofitClient.instance.ubahPassword(token, deviceId, request)

                if (response.isSuccessful) {
                    val pesan = response.body()?.message ?: "Password berhasil diubah!"
                    Toast.makeText(this@UbahPasswordActivity, pesan, Toast.LENGTH_SHORT).show()

                    finish()
                } else {
                    Toast.makeText(this@UbahPasswordActivity, "Gagal merubah password. Cek password lama Anda.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UbahPasswordActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}