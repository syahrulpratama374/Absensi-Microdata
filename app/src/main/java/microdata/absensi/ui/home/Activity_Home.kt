package microdata.absensi.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import microdata.absensi.R
import microdata.absensi.ui.absen.AbsenMasukFragment
import microdata.absensi.ui.absen.AbsenPulangFragment
import microdata.absensi.ui.izin.IzinFragment
import microdata.absensi.ui.profile.ProfileFragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AbsenMasukFragment())
                .commit()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.menu_absen_masuk -> AbsenMasukFragment()
                R.id.menu_absen_pulang -> AbsenPulangFragment()
                R.id.menu_izin -> IzinFragment()
                R.id.menu_profile -> ProfileFragment()
                else -> AbsenMasukFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }
}
