package com.cubex.contactsapp.retrofit.services

import com.cubex.contactsapp.retrofit.model.ContactResponse
import retrofit2.http.GET

interface ContactApiService {
    @GET("api/contacts")
    suspend fun getContacts(): ContactResponse
}