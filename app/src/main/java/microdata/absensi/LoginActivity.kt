package microdata.absensi.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.data.model.LoginRequest
import microdata.absensi.data.remote.RetrofitClient

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etNik = findViewById<TextInputEditText>(R.id.etUser)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etNik.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil Device ID
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            // Tembak API
            doLogin(username, password, deviceId)
        }
    }

    private fun doLogin(username: String, pass: String, deviceId: String) {
        lifecycleScope.launch {
            try {
                val request = LoginRequest(username, pass, deviceId)
                val response = RetrofitClient.instance.login(deviceId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val otpToken = body?.otpToken

                    Toast.makeText(this@LoginActivity, "OTP Terkirim!", Toast.LENGTH_SHORT).show()

                    // Pindah ke halaman OTP bawa token & device ID
                    val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                    intent.putExtra("OTP_TOKEN", otpToken)
                    intent.putExtra("DEVICE_ID", deviceId)
                    startActivity(intent)

                } else {
                    Toast.makeText(this@LoginActivity, "Login Gagal. Cek kembali data Anda.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}