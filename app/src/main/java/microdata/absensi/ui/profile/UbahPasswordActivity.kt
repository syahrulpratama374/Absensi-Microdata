package microdata.absensi.ui.profile

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.data.model.CekPasswordRequest
import microdata.absensi.data.model.KonfirmasiUbahPasswordRequest
import microdata.absensi.data.remote.RetrofitClient
import microdata.absensi.utils.UtilsSession

class UbahPasswordActivity : AppCompatActivity() {

    private lateinit var layoutPasswordLama: TextInputLayout
    private lateinit var layoutOtp: TextInputLayout
    private lateinit var layoutPasswordBaru: TextInputLayout
    private lateinit var layoutKonfirmasiPassword: TextInputLayout
    private lateinit var tvOtpInfo: TextView
    private lateinit var etPasswordLama: TextInputEditText
    private lateinit var etOtp: TextInputEditText
    private lateinit var etPasswordBaru: TextInputEditText
    private lateinit var etKonfirmasi: TextInputEditText
    private lateinit var btnSimpan: Button

    private var isStep2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubah_password)

        layoutPasswordLama = findViewById(R.id.layoutPasswordLama)
        layoutOtp = findViewById(R.id.layoutOtp)
        layoutPasswordBaru = findViewById(R.id.layoutPasswordBaru)
        layoutKonfirmasiPassword = findViewById(R.id.layoutKonfirmasiPassword)
        tvOtpInfo = findViewById(R.id.tvOtpInfo)
        etPasswordLama = findViewById(R.id.etPasswordLama)
        etOtp = findViewById(R.id.etOtp)
        etPasswordBaru = findViewById(R.id.etPasswordBaru)
        etKonfirmasi = findViewById(R.id.etKonfirmasiPassword)
        btnSimpan = findViewById(R.id.btnSimpanPassword)

        btnSimpan.setOnClickListener {
            if (isStep2) {
                konfirmasiUbahPassword()
            } else {
                cekPasswordLama()
            }
        }
    }

    private fun cekPasswordLama() {
        val passLama = etPasswordLama.text.toString().trim()

        if (passLama.isEmpty()) {
            Toast.makeText(this, "Password lama wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val session = UtilsSession(this)
        val token = session.getToken() ?: ""
        val bearerToken = "Bearer $token"
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = CekPasswordRequest(passLama)
                val response = RetrofitClient.instance.cekPassword(bearerToken, deviceId, request)

                if (response.isSuccessful) {
                    val pesan = response.body()?.message ?: "Password lama benar. Masukkan kode OTP."
                    Toast.makeText(this@UbahPasswordActivity, pesan, Toast.LENGTH_SHORT).show()
                    showStep2()
                } else {
                    Toast.makeText(this@UbahPasswordActivity, "Password lama salah. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UbahPasswordActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSimpan.isEnabled = true
            }
        }
    }

    private fun showStep2() {
        isStep2 = true
        layoutPasswordLama.visibility = View.GONE
        layoutOtp.visibility = View.VISIBLE
        layoutPasswordBaru.visibility = View.VISIBLE
        layoutKonfirmasiPassword.visibility = View.VISIBLE
        tvOtpInfo.visibility = View.VISIBLE
        btnSimpan.text = getString(R.string.simpan_password)
    }

    private fun konfirmasiUbahPassword() {
        val otp = etOtp.text.toString().trim()
        val passBaru = etPasswordBaru.text.toString().trim()
        val konfirmasi = etKonfirmasi.text.toString().trim()

        if (otp.isEmpty() || otp.length < 6) {
            Toast.makeText(this, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
            return
        }

        if (passBaru.isEmpty() || konfirmasi.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (passBaru != konfirmasi) {
            Toast.makeText(this, "Password baru dan konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
            return
        }

        val session = UtilsSession(this)
        val token = session.getToken() ?: ""
        val bearerToken = "Bearer $token"
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = KonfirmasiUbahPasswordRequest(otp, passBaru, konfirmasi)
                val response = RetrofitClient.instance.konfirmasiUbahPassword(bearerToken, deviceId, request)

                if (response.isSuccessful) {
                    val pesan = response.body()?.message ?: "Password berhasil diubah!"
                    Toast.makeText(this@UbahPasswordActivity, pesan, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@UbahPasswordActivity, "Gagal mengubah password. Cek kode OTP Anda.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UbahPasswordActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSimpan.isEnabled = true
            }
        }
    }
}