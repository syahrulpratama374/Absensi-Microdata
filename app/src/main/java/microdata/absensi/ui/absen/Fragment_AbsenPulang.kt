package microdata.absensi.ui.absen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import microdata.absensi.R
import microdata.absensi.data.model.AbsenRequest
import microdata.absensi.data.remote.RetrofitClient
import microdata.absensi.utils.UtilsSession
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AbsenPulangFragment : Fragment() {

    private lateinit var tvKoordinat: TextView
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnAbsen: MaterialButton
    private lateinit var mapView: MapView

    private var lat: Double? = null
    private var lng: Double? = null
    private var marker: Marker? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.all { it }) {
                getLocation()
            } else {
                tvKoordinat.text = "Koordinat: izin lokasi ditolak"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_absen_pulang, container, false)

        tvKoordinat = view.findViewById(R.id.tv_koordinat)
        etKeterangan = view.findViewById(R.id.et_keterangan)
        btnAbsen = view.findViewById(R.id.btn_absen)
        mapView = view.findViewById(R.id.map)

        view.findViewById<TextView>(R.id.tv_tanggal).text =
            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())

        setupMap()

        view.findViewById<MaterialButton>(R.id.btn_muat_lokasi).setOnClickListener {
            getLocation()
        }

        btnAbsen.setOnClickListener {
            submitAbsen()
        }

        if (hasLocationPermission()) {
            getLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        return view
    }

    private fun setupMap() {
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(17.0)
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) {
            mapView.onPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::mapView.isInitialized) {
            mapView.onDetach()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            lat = location.latitude
            lng = location.longitude
            tvKoordinat.text = String.format(
                Locale.getDefault(),
                "Koordinat: %.6f, %.6f",
                location.latitude,
                location.longitude
            )
            updateMap(location)
        } else {
            tvKoordinat.text = "Koordinat: belum didapat, pastikan GPS aktif"
        }
    }

    private fun updateMap(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        mapView.controller.animateTo(geoPoint)

        marker?.let { mapView.overlays.remove(it) }
        marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Lokasi Anda"
            mapView.overlays.add(this)
        }
        mapView.invalidate()
    }

    private fun submitAbsen() {
        val currentLat = lat
        val currentLng = lng

        if (currentLat == null || currentLng == null) {
            Toast.makeText(requireContext(), "Koordinat belum didapat", Toast.LENGTH_SHORT).show()
            return
        }

        val token = UtilsSession(requireContext()).getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Session tidak valid, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val request = AbsenRequest(
            kodeJenis = "PULANG",
            koordinat = String.format(Locale.getDefault(), "%.6f, %.6f", currentLat, currentLng),
            keterangan = etKeterangan.text?.toString()?.trim() ?: "",
            fileBukti = null
        )

        btnAbsen.isEnabled = false
        btnAbsen.text = "Mengirim..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.absen("Bearer $token", deviceId, request)
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Absen berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                    etKeterangan.text?.clear()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Absen gagal: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error koneksi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                btnAbsen.isEnabled = true
                btnAbsen.text = "Absen Pulang"
            }
        }
    }
}