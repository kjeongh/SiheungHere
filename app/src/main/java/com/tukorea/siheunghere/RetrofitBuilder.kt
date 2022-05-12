package com.tukorea.siheunghere

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://naveropenapi.apigw.ntruss.com")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val naverMapApi = retrofit.create(NaverMapApi::class.java)
}