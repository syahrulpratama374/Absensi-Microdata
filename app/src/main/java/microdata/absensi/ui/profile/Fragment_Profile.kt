package microdata.absensi.ui.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import microdata.absensi.data.model.LogoutRequest
import microdata.absensi.data.remote.RetrofitClient
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

        // Ambil ID dari XML
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvId = view.findViewById<TextView>(R.id.tvId)
//        val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
        val btnUbahPassword = view.findViewById<Button>(R.id.btnUbahPassword)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Set teks Profil
        tvName.text = session.getUsername() ?: "Karyawan/Siswa"
        tvId.text = "Username: ${session.getUsername() ?: "-"}"

        // Aksi Tombol
//        btnEditProfile.setOnClickListener {
//            Toast.makeText(requireContext(), "Fitur Edit Profil belum tersedia", Toast.LENGTH_SHORT).show()
//        }

        btnUbahPassword.setOnClickListener {
            startActivity(Intent(requireContext(), UbahPasswordActivity::class.java))
        }

        btnLogout.setOnClickListener {
            logout()
        }

        return view
    }

    private fun logout() {
        val session = UtilsSession(requireContext())
        val refreshToken = session.getRefreshToken()

        lifecycleScope.launch {
            // Tembak API Logout kalau ada refresh token-nya
            if (!refreshToken.isNullOrEmpty()) {
                try {
                    val deviceId = Settings.Secure.getString(
                        requireContext().contentResolver,
                        Settings.Secure.ANDROID_ID
                    )

                    val request = LogoutRequest(refreshToken, deviceId)
                    RetrofitClient.instance.logout(request)
                } catch (e: Exception) {
                    // Kalau gagal hit server, abaikan dan tetap lanjut logout lokal
                }
            }

            // Hapus sesi lokal
            session.clearSession()

            // Kembali ke halaman Login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}