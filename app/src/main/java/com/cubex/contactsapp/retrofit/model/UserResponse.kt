package com.cubex.contactsapp.retrofit.model


data class ContactResponse(
    val success: Boolean,
    val Data: ContactData
)

data class ContactData(
    val date: String,
    val totalUsers: Int,
    val users: List<User>
)
