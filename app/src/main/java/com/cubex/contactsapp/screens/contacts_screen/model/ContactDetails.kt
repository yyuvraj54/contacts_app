package com.cubex.contactsapp.screens.contacts_screen.model


data class ContactDetails(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>,
    val emails: List<String>,
    val photoUri: String = "",
    val organization: String = "",
    val note: String = ""
)
