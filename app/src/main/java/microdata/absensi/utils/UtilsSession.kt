package microdata.absensi.utils

import android.content.Context
import android.content.SharedPreferences

class UtilsSession(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("AbsensiPrefs", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    // Simpan token pas berhasil OTP
    fun saveToken(token: String, refreshToken: String? = null) {
        editor.putString("ACCESS_TOKEN", token)
        if (!refreshToken.isNullOrEmpty()) {
            editor.putString("REFRESH_TOKEN", refreshToken)
        }
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    fun saveUsername(username: String) {
        editor.putString("USERNAME", username)
        editor.apply()
    }

    fun getUsername(): String? {
        return prefs.getString("USERNAME", null)
    }

    // Cek apakah user udah login
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    // Ambil token (buat dikirim ke API presensi nanti)
    fun getToken(): String? {
        return prefs.getString("ACCESS_TOKEN", null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString("REFRESH_TOKEN", null)
    }

    // Buat fungsi Logout
    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}