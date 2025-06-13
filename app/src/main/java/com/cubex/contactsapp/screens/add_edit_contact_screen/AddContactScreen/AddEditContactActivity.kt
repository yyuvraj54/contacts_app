package com.cubex.contactsapp.screens.add_edit_contact_screen.AddContactScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cubex.contactsapp.screens.add_edit_contact_screen.AddEditContactScreen
import com.cubex.contactsapp.ui.theme.ContactsAppTheme

class AddEditContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactsAppTheme {
                AddEditContactScreen(isEditMode = false)
            }
        }
    }
}

