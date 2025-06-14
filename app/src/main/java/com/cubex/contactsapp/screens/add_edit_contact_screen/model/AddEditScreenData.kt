package com.cubex.contactsapp.screens.add_edit_contact_screen.model

import com.cubex.contactsapp.retrofit.model.User

data class AddEditScreenData(
    val screenTitle: String,
    val contactList: User,
)