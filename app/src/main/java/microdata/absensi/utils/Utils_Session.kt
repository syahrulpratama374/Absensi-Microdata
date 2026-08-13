package microdata.absensi.utils

import android.content.Context
import android.content.SharedPreferences

class UtilsSession(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun saveLogin() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    companion object {
        private const val KEY_LOGGED_IN = "is_logged_in"
    }
}