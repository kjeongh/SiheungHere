package com.tukorea.siheunghere

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


interface NaverMapApi {
    @Headers(
        "X-NCP-APIGW-API-KEY-ID: ${BuildConfig.CLIENT_ID}",
        "X-NCP-APIGW-API-KEY: ${BuildConfig.CLIENT_SECRET}"
    )
    @GET("/map-geocode/v2/geocode")
    open fun searchAddress(@Query("query") query: String?): Call<GeoResponse>

    @GET("/map-reversegeocode/v2/gc")
    fun reverseGeo(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId : String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret : String,
        @Query("coords") coords : String,
        @Query("output") output : String,
        @Query("orders") orders : String
    ): Call<ReverseGeoResponse>
}