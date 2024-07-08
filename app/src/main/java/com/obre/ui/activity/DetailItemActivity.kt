package com.obre.ui.activity

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.obre.R
import com.obre.databinding.ActivityDetailItemBinding
import com.obre.ui.fragment.ProfileFragment
import com.obre.ui.maps.MapsActivity
import com.obre.ui.recycler.Toko
import com.obre.ui.recycler.Usaha
import com.squareup.picasso.Picasso

class DetailItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailItemBinding
    private lateinit var firestore: FirebaseFirestore

    private var jumlahPesanan = 0
    private var totalBiaya = 0

    private var idJasa : String? = null
    private var namaJasa : String? = null
    private var deskripsiJasa : String? = null
    private var biayaJasa : Int? = null
    private var pemilikJasa : String? = null
    private var kategoriJasa : String? = null
    private var batasLayanan : Int? = null
    private var jumlahLayanan : Int? = null
    private var phoneNumber : String? = null
    private var storeAddress: String? = null
    private var photoStore : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            if (jumlahPesanan == 0){
                tvDetailTotalPrice.setVisibility(View.GONE)
            }
        }

        val namaUsaha = intent.getStringExtra("usaha")

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        phoneNumber = sharedPref.getString("USER_PHONENUMBER", "")

        val buttonOrderDetail : Button = findViewById(R.id.button_detailPesan)
        buttonOrderDetail.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_toolbar))

        val toolbar : Toolbar = findViewById(R.id.item_detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = namaUsaha
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        val db = Firebase.firestore
        val layananJasaCollection = db.collection("LayananJasa")

        layananJasaCollection.whereEqualTo("namaLayanan", namaUsaha)
            .get()
            .addOnSuccessListener { documents ->
                for (documents in documents) {
                    val usaha = documents.toObject(Usaha::class.java)

                    idJasa = documents.id
                    namaJasa = usaha.namaLayanan
                    deskripsiJasa = usaha.deskripsi
                    biayaJasa = usaha.biayaJasa
                    pemilikJasa = usaha.pemilik
                    kategoriJasa = usaha.kategori
                    batasLayanan = usaha.batasPelayananSehari
                    jumlahLayanan = usaha.jumlahPelayananSaatIni
                    photoStore = usaha.photoUrl

                    val firestore = FirebaseFirestore.getInstance()
                    val collectionRef = firestore.collection("LayananJasa").document(idJasa.toString()).collection("Rating")

                    collectionRef.get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                return@addOnSuccessListener
                            }

                            var totalNilai = 0
                            var jumlahDokumen = 0

                            for (document in documents) {
                                val nilai = document.getLong("rating")
                                if (nilai != null) {
                                    totalNilai += nilai.toInt()
                                    jumlahDokumen++
                                }
                            }

                            if (jumlahDokumen > 0) {
                                val rataRata = totalNilai.toDouble() / jumlahDokumen.toDouble()
                                val rataRataText = String.format("%.1f", rataRata)
                                binding.tvDetailServiceRating.text = rataRataText
                            } else {
                                binding.tvDetailServiceRating.text = " "
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Gagal mengambil data
                            Log.w(TAG, "Error getting documents: ", exception)
                        }

                    findViewById<TextView>(R.id.tv_detailServiceName).text = namaJasa
                    findViewById<TextView>(R.id.tv_detailServiceDecription).text = deskripsiJasa
                    findViewById<TextView>(R.id.tv_detailHargaSatuan).text = "Rp ${biayaJasa.toString()}"

                    val db = Firebase.firestore
                    db.collection("UserMitra")
                        .document(usaha.idOwner.toString())
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val alamat = documentSnapshot.toObject<Toko>()
                                val lati = alamat?.latitude
                                val longi = alamat?.longitude
                                val replaceAddress = alamat?.alamat
                                    ?.replace("Kecamatan", "")
                                    ?.replace("kecamatan", "")
                                    ?.replace("Kabupaten", "")
                                    ?.replace("kabupaten", "")
                                    ?.replace("Provinsi", "")
                                    ?.replace("provinsi", "")
                                storeAddress = replaceAddress
                                val spannableString = SpannableString(storeAddress)
                                storeAddress?.let { spannableString.setSpan(UnderlineSpan(), 0, it.length, 0) }

                                findViewById<TextView>(R.id.tv_detailAlamat).text = spannableString

                                binding.lyLokasiLayanan.setOnClickListener {
                                    val intent = Intent(this, MapsActivity::class.java).apply {
                                        putExtra("LATITUDE", lati)
                                        putExtra("LONGITUDE", longi)
                                    }
                                    startActivity(intent)
                                }

                                binding.buttonDetailPesan.setOnClickListener {
                                    if (!phoneNumber.isNullOrEmpty()) {
                                        db.collection("RiwayatPesanan")
                                            .whereEqualTo("idLayanan", idJasa)
                                            .whereEqualTo("idPelanggan", userUid)
                                            .whereEqualTo("statusPesanan", "Diproses")
                                            .get()
                                            .addOnSuccessListener {query ->
                                                if (query.documents.isEmpty()) {
                                                    if (jumlahPesanan != 0) {
                                                        if ((jumlahLayanan?.plus(jumlahPesanan)) != batasLayanan?.plus(1)) {
                                                            if ((jumlahLayanan!! + jumlahPesanan >= batasLayanan!! + 1)){
                                                                Toast.makeText(this, "Pesanan melebihi batas kuota yang tersisa.\n Tersisa ${batasLayanan?.minus(
                                                                    jumlahLayanan!!
                                                                )}", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                val intent = Intent(this, ConfirmOrderActivity::class.java).apply {
                                                                    putExtra("pesananJasaId", idJasa)
                                                                    putExtra("pesananNamaJasa", namaUsaha)
                                                                    putExtra("pesananJumlahPesanan", jumlahPesanan)
                                                                    putExtra("pesananTotalPesanan", totalBiaya)
                                                                    putExtra("pesananSatuanBiaya", biayaJasa)
                                                                    putExtra("kategoriLayanan", kategoriJasa)
                                                                    putExtra("longitudeLayanan", longi)
                                                                    putExtra("latitudeLayanan", lati)
                                                                }
                                                                startActivity(intent)
                                                            }
                                                        } else {
                                                            Toast.makeText(this, "Pesanan melebihi batas kuota.\n Tersisa ${batasLayanan?.minus(
                                                                jumlahLayanan!!
                                                            )}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(this, "Harap isi jumlah pesanan!!. Tersisa ${batasLayanan?.minus(
                                                            jumlahLayanan!!
                                                        )}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }else {
                                                    val context = this
                                                    val builder = AlertDialog.Builder(context)
                                                    builder.setTitle("Pemberitahuan")
                                                    builder.setMessage("Anda sudah pernah memesan layanan ini sebelumnya.\n Harap selesaikan pesanan terlebih dahulu")
                                                    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                                                        finish()
                                                    }
                                                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                                                    builder.show()


                                                }

                                            }.addOnFailureListener { exception ->
                                                // Tindakan jika terjadi kegagalan dalam query
                                            }
                                    }
                                    else {
                                        val context = this
                                        val builder = AlertDialog.Builder(context)
                                        builder.setTitle("Pemberitahuan")
                                        builder.setMessage("Harap isi nomor telepon anda pada halaman akun terlebih dahulu.")
                                        builder.setPositiveButton(R.string.tambah) { dialog, which ->
                                            openMainActivityWithFragment("account")
                                        }
                                        builder.setNegativeButton(android.R.string.cancel) {dialog, which ->
                                            finish()
                                        }
                                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                        builder.show()
                                    }
                                }

                                binding.buttonDetailAdd.setOnClickListener{
                                    jumlahPesanan++
                                    jumlahPesanan()
                                    if (jumlahPesanan != 0){
                                        binding.tvDetailTotalPrice.setVisibility(View.VISIBLE)
                                    }
                                    totalBiaya()
                                }

                                binding.buttonDetailRemove.setOnClickListener {
                                    if (jumlahPesanan > 0 ) {
                                        jumlahPesanan--
                                        if (jumlahPesanan == 0){
                                            binding.tvDetailTotalPrice.setVisibility(View.GONE)
                                        }
                                        jumlahPesanan()
                                        totalBiaya()
                                    }
                                }

                            } else {
                                Log.d(TAG, "Document not exists")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting document", exception)
                        }

                    if (!photoStore.isNullOrEmpty()) {
                        Picasso.get().load(photoStore).resize(300, 300).centerCrop().into(binding.ivDetailServiceImage)
                    } else {
                        Picasso.get().load(R.drawable.obre_logo_small).resize(300, 300).centerCrop().into(binding.ivDetailServiceImage)
                    }
                }
            }
            .addOnFailureListener{exception ->
                Log.w(TAG, "Error getting documents", exception)
            }
    }

    private fun openMainActivityWithFragment(fragmentName: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment_to_open", fragmentName)
        startActivity(intent)
    }

    private fun jumlahPesanan() {
        binding.tvDetailJumlahPesan.text = jumlahPesanan.toString()
    }

    private fun hitungTotal (jumlahPesanan: Int, hargaSatuan: Int): Int {
        return jumlahPesanan * hargaSatuan
    }

    private fun totalBiaya(){
        totalBiaya = biayaJasa?.let { hitungTotal(jumlahPesanan, it) }!!
        binding.tvDetailTotalPrice.text = totalBiaya.toString()
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