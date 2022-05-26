package com.tukorea.siheunghere

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    fun getRetrofit(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun getApiService() : NaverMapApi {
        return getRetrofit().create(NaverMapApi::class.java)
    }

}