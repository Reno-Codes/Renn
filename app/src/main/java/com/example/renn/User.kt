package com.example.renn

import com.google.android.gms.maps.model.LatLng

// User data class
data class User(
    val email: String? = null,
    val userid: String? = null,

    val userStreetName: String? = null,
    val userHouseNumber: String? = null,
    val userPostalCode: String? = null,
    val userCity: String? = null,
    val userCountry: String? = null,
    val userFullAddress: String? = null,
    val userLocation: LatLng? = null,
    val userCircleRadius: Double? = null,

    val workEnabled: Boolean? = false
)

data class UserLocation(
    val userStreetName: String? = null,
    val userHouseNumber: String? = null,
    val userPostalCode: String? = null,
    val userCity: String? = null,
    val userCountry: String? = null,
    val userFullAddress: String? = null,
    val userLocation: LatLng? = null,
    val userCircleRadius: Double? = null
)

// User data class categories
data class Categories(
    val beautyCat: Boolean? = null,
    val homeCat: Boolean? = null,
    val taxiCat: Boolean? = null
)

val CategoryNameAndDescription: Map<String, String> = mapOf(
        "Beauty" to "Schedule any beauty session",
        "Transportation" to "Get a ride for you or your things",
        "Construction Works" to "Get things made, built or fixed"
        )

val CategoryIcons: Map<String, String> = mapOf(
    "Beauty" to "@raw/category_icon_beauty",
    "Transportation" to "@raw/category_icon_transportation",
    "Construction Works" to "@raw/category_icon_construction",
)
