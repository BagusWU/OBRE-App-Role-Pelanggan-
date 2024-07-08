package com.obre.ui.fragment

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.obre.R
import com.obre.databinding.FragmentHistoryBinding
import com.obre.ui.adapter.RiwayatAdapter
import com.obre.ui.recycler.RiwayatPesanan
import com.obre.utils.ItemSpacing

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var riwayatAdapter : RiwayatAdapter
    private lateinit var riwayatList : List<RiwayatPesanan>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        db.collection("RiwayatPesanan")
            .whereEqualTo("idPelanggan", userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.w(TAG, "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val serviceItem = snapshot.documents.map { document ->
                        val uid = document.id
                        val history = document.toObject(RiwayatPesanan::class.java)!!
                        history.idPesanan = uid
                        history
                    }
                    showItems(serviceItem)
                } else {
                    Log.d(TAG, "No such document")
                }
            }



        recyclerView = requireView().findViewById(R.id.recylerview_history)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemSpacingDecoration = ItemSpacing(16)
        recyclerView.addItemDecoration(itemSpacingDecoration)
    }

    private fun showItems(services: List<RiwayatPesanan>) {
        val sorterRiwayat = services.sortedByDescending { it.tanggalPesanan }
        riwayatAdapter = RiwayatAdapter(sorterRiwayat)
        recyclerView.adapter = riwayatAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}