package com.obre.ui.recycler

import android.os.Parcel
import android.os.Parcelable

data class Usaha(
    val idOwner: String?= null,
    var namaLayanan: String ?= null,
    var pemilik: String ?= null,
    var kategori: String ?= null,
    var biayaJasa: Int ?= null,
    var deskripsi: String ?= null,
    var antrian: List<Map<String, Any>>? = null,
    var batasPelayananSehari: Int? = null,
    var jumlahPelayananSaatIni: Int? = null,
    var phoneNumberService: String? = null,
    var photoUrl: String? = null,
    var alamat: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        TODO("antrian"),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idOwner)
        parcel.writeString(namaLayanan)
        parcel.writeString(pemilik)
        parcel.writeString(kategori)
        parcel.writeValue(biayaJasa)
        parcel.writeString(deskripsi)
        parcel.writeValue(batasPelayananSehari)
        parcel.writeValue(jumlahPelayananSaatIni)
        parcel.writeString(phoneNumberService)
        parcel.writeString(photoUrl)
        parcel.writeString(alamat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Usaha> {
        override fun createFromParcel(parcel: Parcel): Usaha {
            return Usaha(parcel)
        }

        override fun newArray(size: Int): Array<Usaha?> {
            return arrayOfNulls(size)
        }
    }
}
