package com.cubex.contactsapp.screens.add_edit_contact_screen.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.cubex.contactsapp.retrofit.model.User
import com.cubex.contactsapp.screens.add_edit_contact_screen.model.AddEditScreenData
import com.cubex.contactsapp.screens.add_edit_contact_screen.viewmodel.ContactFormViewModel
import com.cubex.contactsapp.ui.theme.ContactsAppTheme
import com.google.gson.Gson


class AddEditContactActivity : ComponentActivity() {

    private val viewModel by viewModels<ContactFormViewModel>()

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val imagePath = getRealPathFromURI(it) ?: it.toString()
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
                screenTitle = if (isEditMode) "Edit Contact" else "Add Contact",
                contactList = user
            )
        }

        contactData?.let {
            viewModel.loadContact(it)
        }

        val screenData = if (isEditMode) {
            AddEditScreenData(
                "Edit Contact",
                User(
                    id = intent.getStringExtra("userId") ?: "",
                    fullName = intent.getStringExtra("fullName") ?: "",
                    phone = intent.getStringExtra("phone") ?: "",
                    email = intent.getStringExtra("email") ?: "",
                    course = intent.getStringExtra("course") ?: "",
                    profileImage = intent.getStringExtra("profileImage")

                )
            )
        } else null



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
    private fun getRealPathFromURI(uri: Uri): String? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(android.provider.MediaStore.Images.Media.DATA)
                    if (columnIndex != -1) {
                        it.getString(columnIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
