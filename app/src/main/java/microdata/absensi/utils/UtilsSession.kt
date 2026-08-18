package microdata.absensi.utils

import android.content.Context
import android.content.SharedPreferences

class UtilsSession(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("AbsensiPrefs", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    // Simpan token pas berhasil OTP
    fun saveToken(token: String) {
        editor.putString("ACCESS_TOKEN", token)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    // Cek apakah user udah login
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    // Ambil token (buat dikirim ke API presensi nanti)
    fun getToken(): String? {
        return prefs.getString("ACCESS_TOKEN", null)
    }

    // Buat fungsi Logout
    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}