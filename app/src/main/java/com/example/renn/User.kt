package com.example.renn

import com.google.android.gms.maps.model.LatLng

data class User(
    val email: String? = null,
    val userid: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val workEnabled: Boolean? = null
)

data class Categories(
    val homeCat: Boolean? = null,
    val taxiCat: Boolean? = null
)