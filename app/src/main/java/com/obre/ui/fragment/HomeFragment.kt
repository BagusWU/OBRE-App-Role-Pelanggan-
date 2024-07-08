package com.obre.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.obre.ui.activity.MainActivity
import com.obre.R
import com.obre.databinding.FragmentHomeBinding
import com.obre.ui.activity.ListItemActivity
import com.obre.ui.adapter.MainAdapter
import com.obre.ui.recycler.Usaha
import com.obre.utils.Location
import java.io.IOException
import java.lang.reflect.InvocationTargetException


class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mainAdapter : MainAdapter
    private lateinit var usahaArrayList : ArrayList<Usaha>
    private lateinit var db : FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    private var userName : String? = null
    private var longitude : Double? = null
    private var latitude : Double? = null
    private var userPhone : String? = null

    private var _binding: FragmentHomeBinding? = null
    private val getBinding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                latitude = location.latitude
                longitude = location.longitude

                val context = context ?: return
                val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

                with(sharedPref.edit()) {
                    longitude?.let { putFloat("LONGITUDE", it.toFloat()) }
                    latitude?.let { putFloat("LATITUDE", it.toFloat()) }
                    apply()
                }

                try {
                    val address = Location.getAddressFromLocation(requireContext(), latitude!!, longitude!!)

                    val locationText = if (address.isNotEmpty()) {
                        "$address"
                    } else {
                        "Latitude: $latitude Longitude: $longitude"
                    }
                    view?.findViewById<TextView>(R.id.tv_userlocation)?.text = locationText
                } catch (e: IOException) {

                } catch (e: InvocationTargetException) {

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return getBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseDb = FirebaseFirestore.getInstance()
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        val sharedPref = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        progressBar = _binding!!.progressbarHome
        showLoading()


        if (userUid != null) {
            firebaseDb.collection("UserPelanggan").document(userUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        hideLoading()
                        userName = document.getString("username")
                        userPhone = document.getString("phoneNumber")

                        with(sharedPref.edit()) {
                            putString("USER_USERNAME", userName)
                            putString("USER_PHONENUMBER", userPhone)
                            apply()
                        }

                        view.findViewById<TextView>(R.id.tv_userusername)?.text = "Selamat datang, $userName"
                    } else {
                        hideLoading()
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
        }


        with(sharedPref.edit()) {
            putString("USER_ID", userUid)
            apply()
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            startLocationUpdates()
        }

        recyclerView = requireView().findViewById(R.id.recylerview_main)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        usahaArrayList = ArrayList()
        val categories = usahaArrayList.distinctBy { it.kategori }

        mainAdapter = MainAdapter(categories as ArrayList<Usaha>)

        mainAdapter.setOnItemClickListener { category ->
            val intent = Intent(requireContext(), ListItemActivity::class.java)
            intent.putExtra("category", category.kategori)
            startActivity(intent)
        }

        recyclerView.adapter = mainAdapter

        EventChangeListener()

    }


    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("LayananJasa").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        usahaArrayList.add(dc.document.toObject(Usaha::class.java))
                    }
                    val uniqueCategories = usahaArrayList.distinctBy { it.kategori }
                    displayService(uniqueCategories)
                }
                mainAdapter.notifyDataSetChanged()
            }

        })
    }

    private fun showLoading() {
        if (_binding != null && _binding?.overlayLayout != null) {
            _binding?.overlayLayout?.setOnTouchListener { _, _ -> true }
            _binding?.progressbarHome?.visibility = View.VISIBLE
            _binding?.overlayLayout?.visibility = View.VISIBLE
        }
    }

    private fun hideLoading() {
        if (_binding != null && _binding?.overlayLayout != null) {
            _binding?.overlayLayout?.setOnTouchListener { _, _ -> false }
            _binding?.progressbarHome?.visibility = View.GONE
            _binding?.overlayLayout?.visibility = View.GONE
        }
    }

    private fun displayService(services: List<Usaha>) {
        mainAdapter.setItem(services)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Izin lokasi tidak diberikan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Akses lokasi diperlukan untuk aplikasi ini",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(requireContext(), "Failed to request location settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 1001
        private const val REQUEST_CHECK_SETTINGS = 1002
    }
}