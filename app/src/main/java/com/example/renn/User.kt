package com.example.renn

import com.google.android.gms.maps.model.LatLng

// User data class
data class User(
    val email: String? = null,
    val userid: String? = null,
    val userLocation: LatLng? = null,
    val workEnabled: Boolean? = false
)

// User data class categories
data class Categories(
    val beautyCat: Boolean? = null,
    val homeCat: Boolean? = null,
    val taxiCat: Boolean? = null
)

data class allCategories(
    val numbersMap: Map<String, String> = mapOf(
        "Beauty" to "Schedule any beauty session",
        "Transportation" to "Get a ride for you or your things",
        "Construction Works" to "Get things made, built or fixed"
        )
)