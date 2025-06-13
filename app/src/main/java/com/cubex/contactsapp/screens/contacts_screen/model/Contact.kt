package com.cubex.contactsapp.screens.contacts_screen.model
data class Contact(
    val id: String,
    val name: String,
    val number: String,
    val email: String = "",
    val photoUri: String = ""
)
