package com.example.renn

data class User(
    val email : String? = null,
    val userid: String? = null,
    val workEnabled: Boolean? = null
)

data class Categories(
    val homeCat: String? = null,
    val taxiCat: String? = null
)