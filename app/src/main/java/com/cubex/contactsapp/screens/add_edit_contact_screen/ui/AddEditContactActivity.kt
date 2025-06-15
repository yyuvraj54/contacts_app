package com.cubex.contactsapp.screens.add_edit_contact_screen.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import com.cubex.contactsapp.R
import com.cubex.contactsapp.Utilities
import com.cubex.contactsapp.retrofit.model.User
import com.cubex.contactsapp.screens.add_edit_contact_screen.model.AddEditScreenData
import com.cubex.contactsapp.screens.add_edit_contact_screen.viewmodel.ContactFormViewModel
import com.cubex.contactsapp.app_theme.theme.ContactsAppTheme
import com.google.gson.Gson


class AddEditContactActivity : ComponentActivity() {

    private val viewModel by viewModels<ContactFormViewModel>()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val imagePath = Utilities.getRealPathFromURI(this,it) ?: it.toString()
            viewModel.updateProfileImage(imagePath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isEditMode = intent.getBooleanExtra("is_edit_mode", false)
        val userJson = intent.getStringExtra("user_data")
        val contactData = userJson?.let {
            val user = Gson().fromJson(it, User::class.java)
            AddEditScreenData(
                screenTitle = getString(
                    if (isEditMode) R.string.title_edit_contact else R.string.title_add_contact
                ),
                contactList = user
            )
        }

        contactData?.let {
            viewModel.loadContact(it)
        }

        setContent {
            ContactsAppTheme {
                AddEditContactScreen(
                    isEditMode = isEditMode,
                    screenData = contactData,
                    onImagePickerClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onSaveAndExit = {
                        if(contactData != null) {

                            val updatedUser = viewModel.getUpdatedUser(contactData.contactList)
                            val resultIntent = Intent().apply {
                                putExtra("updatedUser", updatedUser)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                )
            }
        }
    }

}
