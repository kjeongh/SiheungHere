package com.tukorea.siheunghere

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


interface NaverMapApi {
    @Headers(
        "X-NCP-APIGW-API-KEY-ID: ${BuildConfig.CLIENT_ID}",
        "X-NCP-APIGW-API-KEY: ${BuildConfig.CLIENT_SECRET}"
    )
    @GET("/map-geocode/v2/geocode")
    open fun searchAddress(@Query("query") query: String?): Call<GeoResponse>
}