package com.example.asilo360.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit centralizado para consumir la API REST en PHP (XAMPP / MySQL).
 */
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2/asilo360_api/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
