package com.obre.ui.recycler

import android.os.Parcel
import android.os.Parcelable

data class Toko(
    val namaUsaha: String ?= null,
    val deskripsiSingkat: String ?=null,
    val latitude : Double ?= null,
    val longitude : Double ?= null,
    val namaPemilik : String ? =null,
    val phoneNumber : String ?= null,
    val photoUrl : String ?= null,
    val alamat : String ?= null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(namaUsaha)
        parcel.writeString(deskripsiSingkat)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeString(namaPemilik)
        parcel.writeString(phoneNumber)
        parcel.writeString(photoUrl)
        parcel.writeString(alamat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Toko> {
        override fun createFromParcel(parcel: Parcel): Toko {
            return Toko(parcel)
        }

        override fun newArray(size: Int): Array<Toko?> {
            return arrayOfNulls(size)
        }
    }
}
