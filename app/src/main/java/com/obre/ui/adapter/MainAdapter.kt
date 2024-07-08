package com.obre.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.obre.R
import com.obre.ui.recycler.Usaha
import com.squareup.picasso.Picasso

class MainAdapter(private var serviceList: ArrayList<Usaha>) :
    RecyclerView.Adapter<MainAdapter.MyViewHolder>()
{

    private var listener: OnItemClickListener? = null
    private var onItemClickListener: ((Usaha) -> Unit)? = null

    interface OnItemClickListener {
        fun onItemClick(category: Usaha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_main_list, parent, false)
        return  MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val usaha = serviceList[position]
        holder.bind(usaha)
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kategori: TextView = itemView.findViewById(R.id.tv_mainList)
        val photoUrl: ImageView = itemView.findViewById(R.id.iv_mainList)
        val deskripsi: TextView = itemView.findViewById(R.id.tv_mainCategoryDescription)

        fun bind(usaha: Usaha) {
            kategori.text = usaha.kategori

            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("LayananJasa")

            collectionRef
                .whereEqualTo("kategori", usaha.kategori)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val imageUrl = documents.documents[0].getString("photoUrl")
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).into(photoUrl)

                            db.collection("DeskripsiKategori")
                                .whereEqualTo("kategori", usaha.kategori)
                                .get()
                                .addOnSuccessListener {descriptionResult ->
                                    for (document in descriptionResult) {
                                        val deskripsiKategori = document.getString("deskripsi") ?: null
                                        if (deskripsiKategori != null) {
                                            deskripsi.text = deskripsiKategori
                                        } else {
                                            deskripsi.text = ""
                                        }
                                    }
                                }

                        } else {
                           Picasso.get().load(R.drawable.obre_logo_small).into(photoUrl)
                            db.collection("DeskripsiKategori")
                                .whereEqualTo("kategori", usaha.kategori)
                                .get()
                                .addOnSuccessListener {descriptionResult ->
                                    for (document in descriptionResult) {
                                        val deskripsiKategori = document.getString("deskripsi") ?: null
                                        if (deskripsiKategori != null) {
                                            deskripsi.text = deskripsiKategori
                                        } else {
                                            deskripsi.text = ""
                                        }
                                    }
                                }

                        }
                    }
                }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(usaha)
            }
        }

    }

    fun setOnItemClickListener(listener: (Usaha) -> Unit) {
        onItemClickListener = listener
    }

    fun setItem(serviceList: List<Usaha>) {
        this.serviceList = serviceList as ArrayList<Usaha>
        notifyDataSetChanged()
    }

}