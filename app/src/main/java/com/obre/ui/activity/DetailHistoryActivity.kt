package com.obre.ui.activity

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.obre.R
import com.obre.databinding.ActivityDetailHistoryBinding
import com.obre.ui.maps.MapsActivity
import com.obre.ui.recycler.RiwayatPesanan
import com.obre.ui.recycler.Toko
import com.obre.ui.recycler.Usaha
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

class DetailHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailHistoryBinding

    private var namaPemesan : String ?= null
    private var jenisLayanan : String ?= null
    private var metodeBayar : String ?= null
    private var alamatPemesan : String ?= null
    private var jenisUsaha : String ?= null
    private var namaUsaha : String ?= null
    private var totalBiaya : String ?= null
    private var statusPesanan : String ?= null
    private var rating : Boolean ?= null
    private var idLayanan : String ?= null
    private var idPelanggan : String ?= null
    private var idPesanan : String ?= null
    private var longLocation : Double ?= null
    private var latLocation : Double ?= null
    private var phoneNumberService : String ?= null
    private var tanggalPesanan: Timestamp ?= null
    private var biayaOngkir : Long ?= null
    private var jumlahPesanan : Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val antrian : LinearLayout = findViewById(R.id.ll_detailHistoryAntrian)
        val penilaianLayanan : LinearLayout = findViewById(R.id.ll_inputNilaiRating)
        val chatService : TextView = findViewById(R.id.btn_detailHistoryContact)

        idPesanan = intent.getStringExtra("id_Pesanan")

        val toolbar : Toolbar = findViewById(R.id.detail_history_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.detail_pesanan)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }


        val db = FirebaseFirestore.getInstance()
        val documentReferences = db.collection("RiwayatPesanan").document(idPesanan.toString())

        documentReferences.get().addOnSuccessListener { documents ->
            if (documents.exists()) {

                val riwayat = documents.toObject(RiwayatPesanan::class.java)
                namaPemesan = riwayat?.namaPelanggan
                jenisLayanan = riwayat?.jenisPesanan
                metodeBayar = riwayat?.metodeBayar
                alamatPemesan = riwayat?.detailAlamatPelanggan
                jenisUsaha = riwayat?.kategoriLayanan
                namaUsaha = riwayat?.namaLayanan
                totalBiaya = riwayat?.totalBiaya.toString()
                statusPesanan = riwayat?.statusPesanan
                rating = riwayat?.rating
                idLayanan = riwayat?.idLayanan
                idPelanggan = riwayat?.idPelanggan
                longLocation = riwayat?.longitudePelanggan
                latLocation = riwayat?.latitudePelanggan
                phoneNumberService = riwayat?.phoneNumberService
                tanggalPesanan = riwayat?.tanggalPesanan as Timestamp
                biayaOngkir = riwayat?.biayaOngkir
                jumlahPesanan = riwayat?.jumlahPesanan


                binding.apply {
                    tvDetailHostoryOrderId.text = documents.id
                    tvDetailHistoryUsername.text = namaPemesan
                    tvDetailHistoryJenisPelayanan.text = jenisLayanan
                    tvDetailHistorymetodePembayaran.text = metodeBayar
                    tvDetailHistoryAlamatPemesan.text = alamatPemesan
                    tvDetailHistoryKategori.text = jenisUsaha
                    tvDetailHistoryNamaLayanan.text = namaUsaha
                    tvDetailHistoryTotalBiaya.text = "Rp $totalBiaya"
                    tvDetailHistoryStatusPesanan.text = statusPesanan
                    tvDetailHistoryBiayaOngkir.text = "Rp $biayaOngkir"
                    tvDetailHistoryJumlahPesanan.text = jumlahPesanan.toString()

                    val db = Firebase.firestore

                    db.collection("LayananJasa")
                        .document(riwayat.idLayanan.toString())
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val usaha = documentSnapshot.toObject<Usaha>()

                                tvDetailHistoryBiayaLayanan.text = "Rp ${
                                    jumlahPesanan?.let {
                                        usaha?.biayaJasa?.times(
                                            it
                                        )
                                    }
                                }"
                            } else {
                                Log.d(ContentValues.TAG, "Document not exists")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error getting document", exception)
                        }
                }

                binding.btnDetailHistoryMaps.setOnClickListener {
                    val intent = Intent(this, MapsActivity::class.java).apply {
                        putExtra("LATITUDE", latLocation)
                        putExtra("LONGITUDE", longLocation)
                    }
                    startActivity(intent)
                }

                if (riwayat?.statusPesanan.equals("Diproses")) {
                    antrian.setVisibility(View.VISIBLE);
                    penilaianLayanan.setVisibility(View.GONE)
                    chatService.setVisibility(View.VISIBLE)

                    var tanggal = tanggalPesanan!!.toDate()
                    val formatTanggal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tanggal)

                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("LayananJasa").document(idLayanan.toString())

                    val message = "Halo, saya $namaPemesan, pelanggan pada aplikasi OBRE.\nSaya ingin mengajukan pertanyaan pada layanan yang saya pesan dengan id $idPesanan pada tanggal $formatTanggal."

                    binding.btnDetailHistoryContact.setOnClickListener {

                            if (!phoneNumberService.isNullOrBlank()) {
                                if (phoneNumberService?.startsWith("0") == true) {
                                    phoneNumberService = "62" + phoneNumberService!!.substring(1)

                                    callService(phoneNumberService!!, message)
                                } else if (phoneNumberService?.startsWith("+62") == true) {
                                    phoneNumberService = "62" + phoneNumberService!!.substring(3)

                                    callService(phoneNumberService!!, message)
                                }
                            } else {
                                Toast.makeText(this, "Nomor layanan tidak tersedia.", Toast.LENGTH_SHORT).show()
                            }


                    }

                    docRef.get().addOnSuccessListener { document ->
                        val antrian = document["antrian"] as? List<Map<String, Any>> ?: emptyList()

                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        val posisi = antrian.indexOfFirst { it["userId"] == userId }

                        if (posisi != -1) {
                            findViewById<TextView>(R.id.tv_detailHistoryNomorAntrian).text = (posisi +1).toString()
                            findViewById<TextView>(R.id.tv_detailHistoryJumlahTotalAntrian).text = (antrian.size).toString()
                        } else {
                            Log.d("ErrorDebug", "Anda tidak ditemukan dalam antrian")
                        }
                    }.addOnFailureListener { exception ->
                        Log.d("ErrorDebug", "Gagal mendapatkan antrian $exception")
                    }

                } else if (riwayat?.statusPesanan.equals("Selesai")) {
                    antrian.setVisibility(View.GONE)
                    penilaianLayanan.setVisibility(View.VISIBLE)
                    chatService.setVisibility(View.GONE)
                    if (rating == true) {
                        binding.apply {
                            btnSubmitRating.visibility = View.GONE
                            ratingBar.isEnabled = false
                            ratingBar.isEnabled = false
                        }

                        db.collection("LayananJasa").document(idLayanan.toString())
                            .collection("Rating")
                            .whereEqualTo("idPesanan", idPesanan)
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    val rating = document.getDouble("rating")?.toFloat()
                                    val comment = document.getString("comment")

                                    binding.ratingBar.rating = rating ?: 0.0f
                                    binding.etComment.text = Editable.Factory.getInstance().newEditable(comment)
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Handle error
                            }

                    } else {
                        binding.apply {
                            btnSubmitRating.visibility = View.VISIBLE
                            ratingBar.isEnabled = true
                            etComment.isEnabled = true

                            val ratingBar = binding.ratingBar
                            val commentEditText = binding.etComment

                            btnSubmitRating.setOnClickListener {
                                val inputRating = ratingBar.rating.toDouble()
                                val comment = commentEditText.text.toString().trim()
                                Log.d("MyApp", "Rating: $inputRating, Comment: $comment")
                                if (inputRating > 0 && comment.isNotBlank()) {
                                    val ratingData = hashMapOf(
                                        "idPesanan" to idPesanan,
                                        "namaPelanggan" to namaPemesan,
                                        "rating" to inputRating,
                                        "comment" to comment,
                                        "idUsaha" to idLayanan,
                                        "tanggalPesan" to FieldValue.serverTimestamp()
                                    )

                                    idLayanan?.let { idLayanan ->
                                        db.collection("LayananJasa")
                                            .document(idLayanan)
                                            .collection("Rating")
                                            .add(ratingData)
                                            .addOnSuccessListener { documentReference ->
                                                val context = applicationContext
                                                Toast.makeText(context, "Rating berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                                documentReferences?.update("rating", true)
                                                openMainActivityWithFragment("history")
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                val context = applicationContext
                                                Toast.makeText(context, "Gagal menyimpan rating: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }

                                } else {
                                    val context = applicationContext
                                    Toast.makeText(context, "Harap isi dengan benar!!", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                    }
                } else {
                    antrian.setVisibility(View.GONE)
                    penilaianLayanan.setVisibility(View.GONE)
                    chatService.setVisibility(View.GONE)
                }
            }
        }.addOnFailureListener{ exception ->
            Log.w(ContentValues.TAG, "Error getting documents", exception)
        }

    }

    private fun callService (phoneNumberService : String, message : String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumberService&text=${URLEncoder.encode(message, "UTF-8")}")
            `package` = "com.whatsapp"
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp tidak terinstal di perangkat Anda.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMainActivityWithFragment(fragmentName: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment_to_open", fragmentName)
        startActivity(intent)
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
}