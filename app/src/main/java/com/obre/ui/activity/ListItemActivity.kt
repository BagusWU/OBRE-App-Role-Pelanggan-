package com.obre.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.obre.R
import com.obre.databinding.ActivityListItemBinding
import com.obre.ui.adapter.ListItemAdapter
import com.obre.ui.recycler.Usaha

class ListItemActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var listItemAdapter: ListItemAdapter
    private lateinit var usahaList : ArrayList<Usaha>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var binding: ActivityListItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recylerview_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        val category = intent.getStringExtra("category")
        val db = Firebase.firestore

        val toolbar : Toolbar = findViewById(R.id.item_list_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = category
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white)
        }

        db.collection("LayananJasa")
            .whereEqualTo("kategori", category)
            .get()
            .addOnSuccessListener { result ->
                val serviceItem = result.documents.map { it.toObject(Usaha::class.java)!! }
                showItems(serviceItem)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }
    }

    private fun refreshContent() {
        val category = intent.getStringExtra("category")
        val db = Firebase.firestore

        db.collection("LayananJasa")
            .whereEqualTo("kategori", category)
            .get()
            .addOnSuccessListener { result ->
                val serviceItem = result.documents.map { it.toObject(Usaha::class.java)!! }
                showItems(serviceItem)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        swipeRefreshLayout.isRefreshing = false
    }

    private fun showItems(services: List<Usaha>) {
        listItemAdapter =ListItemAdapter(services)
        recyclerView.adapter = listItemAdapter
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

    override fun onResume() {
        super.onResume()
    }

    companion object {
        private const val TAG = "DataItemActivity"
    }
}