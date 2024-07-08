package com.obre.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.obre.R
import com.obre.databinding.ActivityRatingBinding
import com.obre.ui.adapter.RatingAdapter
import com.obre.ui.recycler.Rating
import java.util.Date

class RatingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRatingBinding
    private lateinit var ratingsAdapter: RatingAdapter
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idOwner = intent.getStringExtra("idOwner") ?: ""
        val namaToko = intent.getStringExtra("namaToko") ?: ""

        val toolbar : Toolbar = findViewById(R.id.rating_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = namaToko
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }


        recyclerView = findViewById(R.id.recyclerView_rating)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ratingsAdapter = RatingAdapter()
        recyclerView.adapter = ratingsAdapter

        listRating(idOwner)
    }

    private fun listRating(idOwner: String) {
        db.collection("LayananJasa")
            .whereEqualTo("idOwner", idOwner)
            .get()
            .addOnSuccessListener { documents ->
                val allRatings = mutableListOf<Rating>()
                val tasks = documents.map { document ->
                    val idLayanan = document.id
                    db.collection("LayananJasa")
                        .document(idLayanan)
                        .collection("Rating")
                        .get()
                        .addOnSuccessListener { ratingDocuments ->
                            val ratingsList = ratingDocuments.map { ratingDocument ->
                                Rating(
                                    namaPelanggan = ratingDocument.getString("namaPelanggan") ?: "",
                                    rating = ratingDocument.getDouble("rating")?.toFloat() ?: 0.0f,
                                    comment = ratingDocument.getString("comment") ?: "",
                                    tanggalPesan = ratingDocument.getTimestamp("tanggalPesan") ?: Timestamp(Date(0))
                                )
                            }
                            allRatings.addAll(ratingsList)
                        }
                }
                tasks.forEach { it.addOnCompleteListener { _ ->
                    if (tasks.all { task -> task.isComplete }) {
                        ratingsAdapter.submitList(allRatings)
                    }
                }}
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
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
}