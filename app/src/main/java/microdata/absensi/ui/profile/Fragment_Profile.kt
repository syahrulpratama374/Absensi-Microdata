package microdata.absensi.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.ui.LoginActivity
import microdata.absensi.utils.UtilsSession

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val session = UtilsSession(requireContext())

        view.findViewById<TextView>(R.id.tvName).text =
            session.getUsername() ?: "Karyawan/Siswa"
        view.findViewById<TextView>(R.id.tvId).text =
            "Username: ${session.getUsername() ?: "-"}"

        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Edit Profil belum tersedia", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnUbahPassword).setOnClickListener {
            startActivity(Intent(requireContext(), UbahPasswordActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }

        return view
    }

    private fun logout() {
        val session = UtilsSession(requireContext())
        val refreshToken = session.getRefreshToken()

        lifecycleScope.launch {
            if (!refreshToken.isNullOrEmpty()) {
                try {
                    val deviceId = android.provider.Settings.Secure.getString(
                        requireContext().contentResolver,
                        android.provider.Settings.Secure.ANDROID_ID
                    )
                    microdata.absensi.data.remote.RetrofitClient.instance.logout(
                        microdata.absensi.data.model.LogoutRequest(refreshToken, deviceId)
                    )
                } catch (e: Exception) {
                    // gagal hit server tetap lanjut logout lokal
                }
            }

            session.clearSession()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}