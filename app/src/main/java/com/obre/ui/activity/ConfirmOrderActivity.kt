package com.obre.ui.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.obre.R
import com.obre.databinding.ActivityConfirmOrderBinding
import com.obre.ui.maps.MapsActivity
import java.util.Date
import java.util.UUID
import kotlin.math.ceil

class ConfirmOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmOrderBinding
    private lateinit var db: FirebaseFirestore

    private var serviceId : String? = null
    private var serviceName : String? = null
    private var orderQuantity : Int? = null
    private var orderTotal : Int? = null
    private var unitCost : Int? = null
    private var serviceCategory : String? = null
    private var typeService : String? = null
    private var paymentMethod : String? = null
    private var userId : String? = null
    private var userUsername : String? = null
    private var userLongitude : Double? = null
    private var userLatitude : Double? = null
    private var userAddress: String? = null
    private var userAddressMaps: String? = null
    private var userPhone: String? = null
    private var nomorPesanan : String? = null
    private var serviceLongitude : Double? = null
    private var serviceLatitude : Double? = null
    private var deliveryCost : Double? = null
    private var totalBiaya : Int? = null
    private var biayaOngkir : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonOrderDetail : Button = findViewById(R.id.button_confirmOrder)
        buttonOrderDetail.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_toolbar))

        serviceId = intent.getStringExtra("pesananJasaId")
        serviceName = intent.getStringExtra("pesananNamaJasa")
        orderQuantity = intent.getIntExtra("pesananJumlahPesanan", 0)
        orderTotal = intent.getIntExtra("pesananTotalPesanan", 0)
        unitCost = intent.getIntExtra("pesananSatuanBiaya", 0)
        serviceCategory = intent.getStringExtra("kategoriLayanan")
        serviceLatitude = intent.getDoubleExtra("latitudeLayanan", 0.0)
        serviceLongitude = intent.getDoubleExtra("longitudeLayanan", 0.0)

        typeService = getString(R.string.rumah)
        paymentMethod = getString(R.string.tunai)

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        userPhone = sharedPref.getString("USER_PHONENUMBER", "")
        userId = sharedPref.getString("USER_ID", "")
        userUsername = sharedPref.getString("USER_USERNAME", "")

        Log.d("Debug", "IdJasa: $serviceId")
        Log.d("Debug", "namaJasa: $serviceName")
        Log.d("Debug", "jumlahPesanan: $orderQuantity")
        Log.d("Debug", "totalPesanan: $orderTotal")
        Log.d("Debug", "satuanBiaya: $unitCost")
        Log.d("Debug", "kategoriLayanan: $serviceCategory")
        Log.d("Debug", "UserUid: $userId")
        Log.d("Debug", "Username: $userUsername")
        Log.d("Debug", "Phone: $userPhone")
        Log.d("Debug", "LatService: $serviceLatitude")
        Log.d("Debug", "LongService: $serviceLongitude")

        val toolbar : Toolbar = findViewById(R.id.confirm_order_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = serviceName
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        binding.apply {
            konfirmasiBiayaLayanan.text = "Rp $orderTotal"
            konfirmasiKategori.text = serviceCategory
            konfirmasiNamaUsaha.text = serviceName
            jumlahPesanan.text = orderQuantity.toString()
        }

        binding?.btnConfirmOrderPilihLokasi?.setOnClickListener {
            val intent = Intent(this@ConfirmOrderActivity, MapsActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MAPS)
        }

        binding.buttonConfirmOrder.setOnClickListener {
            userAddress = binding.etUserAddress.text.toString().trim()
            val addressMaps = binding.tvConfirmOrderAddress.text.toString().trim()
            if (userAddress!!.isEmpty()) {
                Toast.makeText(this, "Harap Isi Detail Alamat Anda", Toast.LENGTH_SHORT).show()
            } else{
                if (addressMaps.isEmpty()){
                    Toast.makeText(this, "Harap pilih lokasi anda", Toast.LENGTH_SHORT).show()
                } else {
                    showConfirmationDialog()
                }
            }
        }
    }

    private fun getServiceDeliveryCost(userLatitude: Double, userLongitude: Double) {
        db = FirebaseFirestore.getInstance()
        val docRef = db.collection("PengaturanBiayaTambahan").document("ongkoskirim")

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if (document.exists()) {
                        val ratePerKilometer = document.getDouble("ongkosPerKilometer")
                        if (ratePerKilometer != null) {
                            calculateDeliveryCost(ratePerKilometer, serviceLatitude, serviceLongitude, userLatitude, userLongitude)
                        } else {
                            println("ratePerKilometer not found")
                        }
                    } else {
                        println("Document does not exist")
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    private fun calculateDeliveryCost(ratePerKilometer: Double, startLat : Double?, startLong : Double?, endLat : Double?, endLong : Double?) {

        val distanceInMeters = calculateDistance(startLat, startLong, endLat, endLong)
        val distanceInKilometers = distanceInMeters / 1000

        val deliveryCostRough = distanceInKilometers * ratePerKilometer

        deliveryCost = ceil(deliveryCostRough / 1000) * 1000


        Log.d("Debug", "Jarak: $distanceInKilometers km")
        Log.d("Debug", "Biaya Pengiriman: Rp $deliveryCost")

        totalBiaya = (deliveryCost!! + orderTotal!!).toInt()
        biayaOngkir = deliveryCost!!.toInt()

        binding.konfirmasiTotalBiaya.text = "Rp $totalBiaya"
        binding.konfirmasiBiayaOngkir.text = "Rp $biayaOngkir"
    }

    private fun calculateDistance(
        startLatitude: Double?,
        startLongitude: Double?,
        endLatitude: Double?,
        endLongitude: Double?
    ): Float {
        return if (startLatitude != null && startLongitude != null && endLatitude != null && endLongitude != null) {
            val startLocation = Location("start").apply {
                latitude = startLatitude
                longitude = startLongitude
            }
            val endLocation = Location("end").apply {
                latitude = endLatitude
                longitude = endLongitude
            }
            startLocation.distanceTo(endLocation)
        } else {
            0f
        }
    }



    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Pesanan")
        builder.setMessage("Apakah pesanan sudah sesuai?")
        builder.setPositiveButton("Ya") { dialogInterface: DialogInterface, _: Int ->

            val dbCollection = FirebaseFirestore.getInstance()

            serviceId?.let {
                dbCollection.collection("LayananJasa").document(it)
                    .get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val batasPesananReceive = document.getLong("batasPelayananSehari")
                            val jumlahPelayananSaatIni = document.getLong("jumlahPelayananSaatIni")
                            val idOwner = document.getString("idOwner")
                            val phoneNumberService = document.getString("phoneNumberService")

                            val batasPesanan = batasPesananReceive?.toInt() ?:0
                            val jumlahPelayanan = jumlahPelayananSaatIni?.toInt() ?:0

                            Log.d("Debug", "Batas: $batasPesanan")
                            Log.d("Debug", "Jumlah Layanan: $jumlahPelayanan")

                            if (jumlahPelayanan < batasPesanan) {

                                nomorPesanan = "OBRE-${UUID.randomUUID().toString()}"

                                val tambahPelayanan = (jumlahPelayanan + orderQuantity!!).toLong()

                                val tambahPesanan = hashMapOf(
                                    "jumlahPelayananSaatIni" to tambahPelayanan
                                ).toMap()

                                val db = FirebaseFirestore.getInstance()
                                val docRef = db.collection("LayananJasa").document(it)

                                docRef.get().addOnSuccessListener { document ->
                                    val antrian = document["antrian"] as? MutableList<Map<String, Any?>> ?: mutableListOf()

                                    val posisiPesananTerbaru = antrian.size + 1
                                    for (i in 1..orderQuantity!!) {
                                        val pesanan = mapOf(
                                            "userId" to userId,
                                            "tanggal" to Date().toString(),
                                            "nomorPesanan" to nomorPesanan
                                        )
                                        antrian.add(pesanan)
                                    }

                                    docRef.update("antrian", antrian).addOnSuccessListener {
                                        println("Pesanan baru telah ditambahkan ke antrian. Total antrian sekarang: ${antrian.size}")
                                        println("Anda berada pada antrian ke-$posisiPesananTerbaru")
                                    }.addOnFailureListener { exception ->
                                        println("Gagal memperbarui data antrian: $exception")
                                    }
                                }.addOnFailureListener { exception ->
                                    println("Gagal mendapatkan data antrian: $exception")
                                }

                                dbCollection.collection("LayananJasa").document(it).update(tambahPesanan)

                                val orderService = hashMapOf(
                                    "idPelanggan" to userId,
                                    "nomorPelanggan" to userPhone,
                                    "idLayanan" to serviceId,
                                    "namaPelanggan" to userUsername,
                                    "namaLayanan" to serviceName,
                                    "kategoriLayanan" to serviceCategory,
                                    "jumlahPesanan" to orderQuantity,
                                    "totalBiaya" to totalBiaya,
                                    "biayaOngkir" to biayaOngkir,
                                    "latitudePelanggan" to userLatitude,
                                    "longitudePelanggan" to userLongitude,
                                    "alamatPelangganMaps" to userAddressMaps,
                                    "detailAlamatPelanggan" to userAddress,
                                    "tanggalPesanan" to FieldValue.serverTimestamp(),
                                    "metodeBayar" to getString(R.string.tunai),
                                    "jenisPesanan" to  getString(R.string.rumah),
                                    "statusPesanan" to getString(R.string.diProses),
                                    "rating" to false,
                                    "nomorPesanan" to nomorPesanan,
                                    "idOwner" to idOwner,
                                    "phoneNumberService" to phoneNumberService
                                )

                                dbCollection.collection("RiwayatPesanan").add(orderService)
                                    .addOnSuccessListener { document ->
                                        Toast.makeText(this, "Pesanan berhasil", Toast.LENGTH_SHORT).show()
                                        val secondBuilder = AlertDialog.Builder(this)
                                        secondBuilder.setTitle("Pesanan Diproses")
                                        secondBuilder.setMessage("Tunggu penyedia jasa menghubungimu")
                                        secondBuilder.setPositiveButton("Tutup") { dialog, _ ->
                                            val intent = Intent(this@ConfirmOrderActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            dialog.dismiss()
                                        }
                                        secondBuilder.show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ConfirmOrderActivity", "Error adding order", e)
                                        Toast.makeText(this, "Pesanan gagal", Toast.LENGTH_SHORT).show()
                                    }
                            }

                        } else {
                            Log.d("Debug", "Document not found")
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("TAG", "Error getting document", exception)
                    }
            }

            dialogInterface.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_MAPS && resultCode == RESULT_OK && data != null) {
            val latitude = data.getDoubleExtra("latitude", 0.0)
            val longitude = data.getDoubleExtra("longitude", 0.0)
            var userAddressMap = data.getStringExtra("alamat")

            if (latitude != 0.0 && longitude != 0.0) {
                binding.tvConfirmOrderAddress.setText(userAddressMap.toString())
                getServiceDeliveryCost(latitude, longitude)
                userLatitude = latitude
                userLongitude = longitude
                userAddressMaps = userAddressMap.toString()

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
        private const val REQUEST_CODE_MAPS = 1001
    }
}