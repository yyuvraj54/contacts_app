package com.cubex.contactsapp.retrofit

import com.cubex.contactsapp.retrofit.services.ContactApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://android-dev-assignment.onrender.com/"

    val api: ContactApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ContactApiService::class.java)
    }
}