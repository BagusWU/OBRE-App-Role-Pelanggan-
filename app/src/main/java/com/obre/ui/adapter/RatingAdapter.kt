package com.obre.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.obre.R
import com.obre.ui.recycler.Rating
import java.text.SimpleDateFormat
import java.util.Locale


class RatingAdapter : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {
    private var ratingsList: List<Rating> = listOf()

    fun submitList(list: List<Rating>) {
        ratingsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_rating, parent, false)
        return RatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
       val sortedList = ratingsList.sortedByDescending { it.tanggalPesan }
        holder.bind(sortedList[position])
    }

    override fun getItemCount(): Int = ratingsList.size

    class RatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ratingBarList: RatingBar = itemView.findViewById(R.id.ratingBar_list)
        private val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
        private val namaPelanggan: TextView = itemView.findViewById(R.id.namaPelanggan_listRating)
        private val tanggalPesanan: TextView = itemView.findViewById(R.id.tanggal_listRating)

        fun bind(rating: Rating) {
            ratingBarList.rating = rating.rating!!
            commentTextView.text = rating.comment
            namaPelanggan.text = rating.namaPelanggan

            val dataTanggal = rating.tanggalPesan as Timestamp

            if (dataTanggal != null) {
                var tanggal = dataTanggal.toDate()
                val formatTanggal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tanggal)
                tanggalPesanan.text = formatTanggal
            } else {
                tanggalPesanan.text = " "
            }


        }
    }
}