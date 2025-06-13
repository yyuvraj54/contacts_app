package com.cubex.contactsapp.screens.add_edit_contact_screen

import com.cubex.contactsapp.screens.contacts_screen.model.Contact

data class AddEditScreenData(
    val screenTitle: String,
    val contactList: List<Contact>,
)