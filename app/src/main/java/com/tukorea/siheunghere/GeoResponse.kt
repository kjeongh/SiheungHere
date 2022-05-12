package com.tukorea.siheunghere

data class GeoResponse(
    val addresses: List<Addresses>,
    val errorMessage: String,
    val meta: Meta,
    val status: String
)