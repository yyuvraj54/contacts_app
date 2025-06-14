package com.cubex.contactsapp.retrofit.model

import java.io.Serializable

data class User(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val course: String,
    val profileImage: String? =null
): Serializable