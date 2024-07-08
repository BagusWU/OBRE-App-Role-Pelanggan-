package com.obre.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

object Location {
    fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            val addressStringBuilder = StringBuilder()
            for (i in 0..address.maxAddressLineIndex) {
                addressStringBuilder.append(address.getAddressLine(i)).append("\n")
            }
            addressStringBuilder.toString()
        } else {
            "No address found"
        }
    }
}