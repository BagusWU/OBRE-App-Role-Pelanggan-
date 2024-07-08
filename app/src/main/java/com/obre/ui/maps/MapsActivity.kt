package com.obre.ui.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.ZoomControls
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.obre.R
import com.obre.databinding.ActivityMapsBinding
import com.obre.utils.Location.getAddressFromLocation
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var savedLocation : LatLng? = null
    private var secondAlamat : String? = null
    private lateinit var zoomControls: ZoomControls
    private var isLocationDetected: Boolean = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getMyLastLocation()
            } else {
                Toast.makeText(
                    this@MapsActivity,
                    "Izin lokasi dibutuhkan untuk menampilkan lokasi pengguna",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar : Toolbar = findViewById(R.id.maps_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_activity_maps)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        val btnPilih : Button = findViewById(R.id.btn_simpanLokasi)
        val zoomControl : ZoomControls = findViewById(R.id.zoom_controls)

        val historyLatitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val historyLongitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        val location = LatLng(historyLatitude, historyLongitude)

        if (historyLatitude == 0.0 && historyLongitude == 0.0) {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
            btnPilih.setVisibility(View.VISIBLE)
            zoomControl.setVisibility(View.VISIBLE)
        } else {

            btnPilih.setVisibility(View.GONE)
            zoomControl.setVisibility(View.GONE)
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                mMap = googleMap

                mMap.addMarker(MarkerOptions().position(location).title("Marker in Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        zoomControls = findViewById(R.id.zoom_controls)

        binding.btnSimpanLokasi.setOnClickListener {
            Log.d("Debug", "Button clicked. isLocationDetected: $isLocationDetected, savedLocation: $savedLocation, secondAlamat: $secondAlamat")
            if (isLocationDetected != null || savedLocation != null) {
                val intent = Intent()
                savedLocation?.let { it1 -> getAddressFromLatLng(it1) }
                intent.putExtra("latitude", savedLocation?.latitude)
                intent.putExtra("longitude", savedLocation?.longitude)
                intent.putExtra("alamat", secondAlamat)
                Log.d("Alamat : ", secondAlamat.toString())
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this@MapsActivity, "Tidak ada data lokasi yang tersedia", Toast.LENGTH_SHORT).show()
            }
        }
        getMyLastLocation()
    }

    fun getAddressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressString = address.getAddressLine(0)
                secondAlamat = addressString
                Log.d("Debug", "Alamat ditemukan: $secondAlamat")
            } else {
                Log.d("Debug", "Addresses is empty or null")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Debug", "Geocoder failed", e)
        }
    }

    private fun setupZoomControls() {
        zoomControls.setOnZoomInClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        zoomControls.setOnZoomOutClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomBy(-2.0f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupZoomControls()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            getMyLastLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Lokasi Dipilih")
                    .draggable(true)
            )
            savedLocation = latLng


            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val addressString = address.getAddressLine(0)
                        secondAlamat = addressString
                        Log.d("Debug", "Alamat ditemukan: $secondAlamat")
                    } else {
                        Log.d("Debug", "Addresses is empty")
                    }
                } else {
                    Log.d("Debug", "Addresses is null")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Debug", "Geocoder failed", e)
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        if (::mMap.isInitialized) {
                            val latLng = LatLng(location.latitude, location.longitude)
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("Lokasi Saya")
                            )
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            isLocationDetected = true
                            savedLocation = latLng
                        } else {
                            Toast.makeText(
                                this@MapsActivity,
                                "Harap tunggu beberapa saat.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@MapsActivity,
                            "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLastLocation()
            } else {
                Toast.makeText(
                    this@MapsActivity,
                    "Izin lokasi dibutuhkan untuk menampilkan lokasi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

}