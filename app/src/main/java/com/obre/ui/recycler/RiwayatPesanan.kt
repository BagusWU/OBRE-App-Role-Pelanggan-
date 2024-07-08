package com.obre.ui.recycler

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class RiwayatPesanan(
    var idPesanan: String ?= null,
    val idLayanan: String ?= null,
    val idPelanggan: String ?= null,
    val namaPelanggan: String ?= null,
    val jumlahPesanan: Int ?= null,
    val nomorAntrian: Int ?= null,
    val detailAlamatPelanggan: String ?= null,
    val alamatPelangganMaps: String ?= null,
    val totalBiaya: Int ?= null,
    val statusPesanan: String ?= null,
    val tanggalPesanan: Timestamp ?= null,
    val nomorPelanggan: String ?= null,
    val jenisPesanan: String ?= null,
    val namaLayanan: String ?= null,
    val metodeBayar: String ?= null,
    val kategoriLayanan: String ?= null,
    val rating: Boolean ?= null,
    val nomorPesanan: String ?= null,
    val latitudePelanggan: Double ?= null,
    val longitudePelanggan: Double ?= null,
    val phoneNumberService: String ?=null,
    val biayaOngkir: Long ?= null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idPesanan)
        parcel.writeString(idLayanan)
        parcel.writeString(idPelanggan)
        parcel.writeString(namaPelanggan)
        parcel.writeValue(jumlahPesanan)
        parcel.writeValue(nomorAntrian)
        parcel.writeString(detailAlamatPelanggan)
        parcel.writeString(alamatPelangganMaps)
        parcel.writeValue(totalBiaya)
        parcel.writeString(statusPesanan)
        parcel.writeParcelable(tanggalPesanan, flags)
        parcel.writeString(nomorPelanggan)
        parcel.writeString(jenisPesanan)
        parcel.writeString(namaLayanan)
        parcel.writeString(metodeBayar)
        parcel.writeString(kategoriLayanan)
        parcel.writeValue(rating)
        parcel.writeString(nomorPesanan)
        parcel.writeValue(latitudePelanggan)
        parcel.writeValue(longitudePelanggan)
        parcel.writeString(phoneNumberService)
        parcel.writeValue(biayaOngkir)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RiwayatPesanan> {
        override fun createFromParcel(parcel: Parcel): RiwayatPesanan {
            return RiwayatPesanan(parcel)
        }

        override fun newArray(size: Int): Array<RiwayatPesanan?> {
            return arrayOfNulls(size)
        }
    }
}