package com.cubex.contactsapp.retrofit.model

data class User(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val course: String,
    val enrolledOn: String
)