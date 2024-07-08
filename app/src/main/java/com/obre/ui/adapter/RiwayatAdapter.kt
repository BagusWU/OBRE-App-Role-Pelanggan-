package com.obre.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.obre.R
import com.obre.ui.activity.DetailHistoryActivity
import com.obre.ui.recycler.RiwayatPesanan
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatAdapter(private var historyList: List<RiwayatPesanan>) :
    RecyclerView.Adapter<RiwayatAdapter.MyViewHolder>()
{

        init {
            historyList.sortedByDescending { it.tanggalPesanan }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_order_history, parent, false)
        return  MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val riwayat = historyList[position]

        val dataTanggal = riwayat?.tanggalPesanan as Timestamp
        var tanggal = dataTanggal?.toDate()
        val formatTanggal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tanggal)

        holder.biayaPesanan.text = riwayat.totalBiaya.toString()
        holder.jenisLayanan.text = riwayat.jenisPesanan
        holder.namaUsaha.text = riwayat.namaLayanan
        holder.tanggalPesan.text = formatTanggal
        holder.biayaPesanan.text = riwayat.totalBiaya.toString()
        holder.statusPesanan.text = riwayat.statusPesanan

        if (riwayat.statusPesanan == "Diproses") {
            holder.statusPesanan.setBackgroundColor(Color.parseColor("#FFA500"))
            holder.statusPesanan.setTextColor(Color.BLACK)
        } else if (riwayat.statusPesanan == "Selesai") {
            holder.statusPesanan.setBackgroundColor(Color.parseColor("#00FF00"))
            holder.statusPesanan.setTextColor(Color.WHITE)
        } else {
            holder.statusPesanan.setBackgroundColor(Color.parseColor("#FF0000"))
            holder.statusPesanan.setTextColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            Log.d("CategoryClicked", "Category: ${riwayat.idPesanan}")
            val intent = Intent(holder.itemView.context, DetailHistoryActivity::class.java)
            intent.putExtra("id_Pesanan", riwayat.idPesanan)
            holder.itemView.context.startActivity(intent)
        }


    }

    class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaUsaha: TextView = itemView.findViewById(R.id.tv_serviceName)
        val jenisLayanan: TextView = itemView.findViewById(R.id.tv_serviceType)
        val tanggalPesan: TextView = itemView.findViewById(R.id.tv_orderDate)
        val biayaPesanan: TextView = itemView.findViewById(R.id.tv_serviceCost)
        val statusPesanan: TextView = itemView.findViewById(R.id.tv_orderStatus)
    }

}