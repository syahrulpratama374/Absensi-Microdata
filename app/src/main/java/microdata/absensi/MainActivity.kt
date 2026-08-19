package microdata.absensi

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import microdata.absensi.ui.absen.AbsenMasukFragment
import microdata.absensi.ui.absen.AbsenPulangFragment
import microdata.absensi.ui.izin.IzinFragment
import microdata.absensi.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private var mockDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
        bottomNav.selectedItemId = R.id.menu_absen_masuk
    }

    override fun onResume() {
        super.onResume()
        if (isMockLocationEnabled()) {
            showMockWarning()
            return
        }
        checkLiveLocationForMock()
    }

    private fun isMockLocationEnabled(): Boolean {
        if (Settings.Secure.getInt(contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0) {
            return true
        }
        val legitProviders = setOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
            "fused"
        )
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.allProviders.any { it !in legitProviders }
    }

    private fun checkLiveLocationForMock() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { location ->
            if (isLocationSuspicious(location)) {
                showMockWarning()
            }
        }
        try {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper())
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
        } catch (_: Exception) {
        }
    }

    private fun isLocationSuspicious(location: Location): Boolean {
        if (location.isMock || location.isFromMockProvider()) {
            return true
        }
        if (location.provider == LocationManager.NETWORK_PROVIDER && location.hasAccuracy() && location.accuracy < 10f) {
            return true
        }
        return false
    }

    private fun showMockWarning() {
        if (mockDialog?.isShowing == true) return
        mockDialog = AlertDialog.Builder(this)
            .setTitle("Fake GPS Terdeteksi")
            .setMessage("Aplikasi mendeteksi kamu memakai Fake GPS. Matikan Fake GPS-nya dulu supaya bisa pakai absensi.")
            .setCancelable(false)
            .setPositiveButton("Matikan & Keluar") { _, _ -> finishAffinity() }
            .create()
            .apply {
                setOnDismissListener { mockDialog = null }
                show()
            }
    }
}