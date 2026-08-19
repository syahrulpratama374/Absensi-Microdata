package microdata.absensi.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.MainActivity
import microdata.absensi.data.model.OtpRequest
import microdata.absensi.data.remote.RetrofitClient
import microdata.absensi.utils.UtilsSession

class OtpActivity : AppCompatActivity() {

    private var otpToken: String = ""
    private var deviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        // 1. Tangkap data OTP Token dan Device ID yang dikirim dari LoginActivity
        otpToken = intent.getStringExtra("OTP_TOKEN") ?: ""
        deviceId = intent.getStringExtra("DEVICE_ID") ?: ""

        val etOtp = findViewById<TextInputEditText>(R.id.etOtp)
        val btnVerifyOtp = findViewById<Button>(R.id.btnVerifyOtp)

        // 2. Aksi pas tombol verifikasi diklik
        btnVerifyOtp.setOnClickListener {
            val otpCode = etOtp.text.toString().trim()

            // Validasi input OTP (misal wajib 6 digit, sesuaikan kalau API lu beda)
            if (otpCode.isEmpty() || otpCode.length < 6) {
                Toast.makeText(this, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jalankan fungsi tembak API
            verifyOtp(otpCode)
        }
    }

    private fun verifyOtp(otpCode: String) {
        lifecycleScope.launch {
            try {
                // Siapkan data request (body API)
                val request = OtpRequest(
                    otp = otpCode,
                    otpToken = otpToken,
                    deviceId = deviceId
                )

                // Tembak API /verify-otp
                val response = RetrofitClient.instance.verifyOtp(deviceId, request)

                if (response.isSuccessful) {
                    // Kalau OTP Bener (HTTP 200 OK)
                    val body = response.body()
                    val accessToken = body?.accessToken ?: ""

                    // Jangan simpan session kalau token kosong dari server
                    if (accessToken.isEmpty()) {
                        Toast.makeText(this@OtpActivity, "Token kosong dari server, coba lagi", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Simpan token ke SharedPreferences pakai UtilsSession
                    val session = UtilsSession(this@OtpActivity)
                    session.saveToken(accessToken, body?.refreshToken)

                    Toast.makeText(this@OtpActivity, "Verifikasi Sukses!", Toast.LENGTH_SHORT).show()

                    // Pindah ke Halaman Utama (MainActivity)
                    val intent = Intent(this@OtpActivity, MainActivity::class.java)
                    // Hapus riwayat halaman (Back stack) biar kalau di-back kaga balik ke halaman OTP lagi
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    // Kalau OTP Salah atau Expired
                    Toast.makeText(this@OtpActivity, "OTP Salah atau sudah Kadaluarsa", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Kalau internet mati atau server API lagi down
                Toast.makeText(this@OtpActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}