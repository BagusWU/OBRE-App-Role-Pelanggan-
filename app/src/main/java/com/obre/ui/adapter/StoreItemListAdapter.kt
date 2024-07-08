package com.obre.ui.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obre.R
import com.obre.ui.activity.DetailItemActivity
import com.obre.ui.custom.CircleImage
import com.obre.ui.recycler.Usaha
import com.squareup.picasso.Picasso

class StoreItemListAdapter(private var serviceList: List<Usaha>) :
    RecyclerView.Adapter<StoreItemListAdapter.ItemDataViewHolder>()
{

    private var onItemClickListener: ((Usaha) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoreItemListAdapter.ItemDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_store_item_list, parent, false)
        return ItemDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreItemListAdapter.ItemDataViewHolder, position: Int) {
        val usaha = serviceList[position]
        holder.bind(serviceList[position])

        holder.itemView.setOnClickListener {
            Log.d("CategoryClicked", "usaha: ${usaha.namaLayanan}")
            val intent = Intent(holder.itemView.context, DetailItemActivity::class.java)
            intent.putExtra("usaha", usaha.namaLayanan)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    inner class ItemDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namaLayanan: TextView = itemView.findViewById(R.id.tv_storeServiceName)
        private val biayaLayanan: TextView = itemView.findViewById(R.id.tv_storeSservicePrice)
        private val sisaKuota: TextView = itemView.findViewById(R.id.tv_storeSisaKuota)
        private val photoUrl: ImageView = itemView.findViewById(R.id.iv_storeItemImage)
        private val owner: TextView = itemView.findViewById(R.id.tv_serviceOwner)

        fun bind(item: Usaha) {
            namaLayanan.text = item.namaLayanan
            biayaLayanan.text = "Rp ${item.biayaJasa.toString()}"
            sisaKuota.text = "Tersisa untuk\n${(item.jumlahPelayananSaatIni?.let { item.batasPelayananSehari?.minus(it) }).toString()} orang"
            owner.text = item.pemilik

            val imageUrl = item.photoUrl
            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(imageUrl)
                    .transform(CircleImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(photoUrl)
            } else {
                Picasso.get()
                    .load(R.drawable.ic_launcher_foreground)
                    .transform(CircleImage())
                    .into(photoUrl)
            }
        }
    }


}