package com.obre.ui.recycler

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Rating(
    val namaPelanggan: String ?= null,
    val rating: Float ?= null,
    val comment: String ?= null,
    val tanggalPesan: Timestamp?= null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(namaPelanggan)
        parcel.writeValue(rating)
        parcel.writeString(comment)
        parcel.writeParcelable(tanggalPesan, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Rating> {
        override fun createFromParcel(parcel: Parcel): Rating {
            return Rating(parcel)
        }

        override fun newArray(size: Int): Array<Rating?> {
            return arrayOfNulls(size)
        }
    }
}