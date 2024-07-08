package com.obre.ui.adapter

import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.obre.R
import com.obre.ui.activity.StoreActivity
import com.obre.ui.custom.CircleImage
import com.obre.ui.recycler.Toko
import com.obre.ui.recycler.Usaha
import com.squareup.picasso.Picasso

class ListItemAdapter(private var serviceList: List<Usaha>) :
    RecyclerView.Adapter<ListItemAdapter.ItemDataViewHolder>()
{

    private var onItemClickListener: ((Usaha) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListItemAdapter.ItemDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_category_list, parent, false)
        return ItemDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListItemAdapter.ItemDataViewHolder, position: Int) {
        val usaha = serviceList[position]
        holder.bind(serviceList[position])

        holder.itemView.setOnClickListener {
            Log.d("CategoryClicked", "idOwner: ${usaha.idOwner}")
            val intent = Intent(holder.itemView.context, StoreActivity::class.java)
            intent.putExtra("idOwner", usaha.idOwner)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    inner class ItemDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namaUsaha: TextView = itemView.findViewById(R.id.tv_serviceName)
        private val pemilikUsaha: TextView = itemView.findViewById(R.id.tv_serviceOwner)
        private val biayaJasa: TextView = itemView.findViewById(R.id.tv_servicePrice)
        private val sisaKuota: TextView = itemView.findViewById(R.id.tv_sisaKuota)
        private val photoUrl: ImageView = itemView.findViewById(R.id.iv_categoryList)
        private val storeAddress: TextView = itemView.findViewById(R.id.tv_serviceAddress)

        fun bind(item: Usaha) {
            namaUsaha.text = item.namaLayanan
            pemilikUsaha.text = item.pemilik
            biayaJasa.text = "Rp ${item.biayaJasa.toString()}"
            sisaKuota.text = "Tersisa untuk\n${(item.jumlahPelayananSaatIni?.let { item.batasPelayananSehari?.minus(it) }).toString()} orang"

            val db = Firebase.firestore

            db.collection("UserMitra")
                .document(item.idOwner.toString())
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val alamat = documentSnapshot.toObject<Toko>()
                        val replaceAddress = alamat?.alamat
                            ?.replace("Kecamatan", "")
                            ?.replace("kecamatan", "")
                            ?.replace("Kabupaten", "")
                            ?.replace("kabupaten", "")
                            ?.replace("Provinsi", "")
                            ?.replace("provinsi", "")
                        storeAddress.text = replaceAddress ?: ""
                    } else {
                        Log.d(TAG, "Document not exists")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting document", exception)
                }

            if (!item.photoUrl.isNullOrEmpty()) {
                Picasso.get().load(item.photoUrl).transform(CircleImage()).into(photoUrl)
            } else {
                Picasso.get().load(R.drawable.ic_launcher_foreground).transform(CircleImage()).into(photoUrl)
            }
        }
    }


}