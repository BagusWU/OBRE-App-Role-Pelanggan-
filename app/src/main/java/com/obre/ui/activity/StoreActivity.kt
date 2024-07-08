package com.obre.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.obre.R
import com.obre.databinding.ActivityStoreBinding
import com.obre.ui.adapter.StoreItemListAdapter
import com.obre.ui.custom.CircleImage
import com.obre.ui.recycler.Toko
import com.obre.ui.recycler.Usaha
import com.squareup.picasso.Picasso

class StoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreBinding
    private lateinit var storeItemAdapter: StoreItemListAdapter
    private lateinit var recyclerView: RecyclerView

    private var namaToko : String ?= null
    private var deskripsiSingkat : String ?= null
    private var photoStore : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idOwner = intent.getStringExtra("idOwner")

        val db = Firebase.firestore
        val storeCollection = db.collection("UserMitra")

        storeCollection
            .document(idOwner.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val store = document.toObject(Toko::class.java)
                    Log.d(this.toString(), "Found")
                    namaToko = store?.namaUsaha
                    deskripsiSingkat = store?.deskripsiSingkat
                    photoStore = store?.photoUrl

                    binding.apply {
                        tvStoreName.text = namaToko
                        tvStoreInformation.text = deskripsiSingkat
                        Picasso.get().load(photoStore).resize(300, 300).into(ivStore)
                    }

                    db.collection("LayananJasa")
                        .whereEqualTo("idOwner", idOwner)
                        .get()
                        .addOnSuccessListener { documents ->
                            var totalRating = 0.0f
                            var numRatings = 0
                            for (document in documents) {
                                val idLayanan = document.id
                                db.collection("LayananJasa")
                                    .document(idLayanan)
                                    .collection("Rating")
                                    .get()
                                    .addOnSuccessListener { ratingDocuments ->

                                        for (ratingDocument in ratingDocuments) {
                                            val rating = ratingDocument.getDouble("rating")?.toFloat() ?: 0.0f
                                            totalRating += rating
                                            numRatings++
                                        }

                                        if (numRatings > 0) {
                                            val averageRating = totalRating / numRatings

                                            binding.tvStoreRating.rating = averageRating

                                            binding.btnStoreRating.setOnClickListener {
                                                val intent = Intent(this@StoreActivity, RatingActivity::class.java)
                                                intent.putExtra("idOwner", idOwner)
                                                intent.putExtra("namaToko", namaToko)
                                                Log.d("Pesanan", "id: $idOwner")
                                                startActivity(intent)
                                            }
                                        } else {
                                            println("No ratings found.")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        println("Error getting rating documents: $exception")
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Error getting documents: $exception")
                        }


                    recyclerView = findViewById(R.id.recylerview_storeItemList)
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(this)

                    db.collection("LayananJasa")
                        .whereEqualTo("idOwner", idOwner)
                        .get()
                        .addOnSuccessListener { result ->
                            val serviceItem = result.documents.map { it.toObject(Usaha::class.java)!! }
                            showItems(serviceItem)

                        }
                        .addOnFailureListener { exception ->
                            Log.w(this.toString(), "Error getting documents: ", exception)
                        }

                } else {
                    Log.d(this.toString(), "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(this.toString(), "Error getting document: ", exception)
            }

        val toolbar : Toolbar = findViewById(R.id.store_page_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = " "
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }
    }

    private fun showItems(services: List<Usaha>) {
        storeItemAdapter = StoreItemListAdapter(services)
        recyclerView.adapter = storeItemAdapter
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