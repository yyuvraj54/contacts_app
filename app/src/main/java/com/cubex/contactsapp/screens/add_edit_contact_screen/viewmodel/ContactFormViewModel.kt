package com.cubex.contactsapp.screens.add_edit_contact_screen.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubex.contactsapp.Utilities
import com.cubex.contactsapp.retrofit.model.User
import com.cubex.contactsapp.screens.add_edit_contact_screen.model.AddEditScreenData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactFormViewModel : ViewModel() {

    var firstName = mutableStateOf(TextFieldValue(""))
    var sureName = mutableStateOf(TextFieldValue(""))
    var company = mutableStateOf(TextFieldValue(""))
    var phone = mutableStateOf(TextFieldValue(""))
    var profileImage = mutableStateOf<String?>(null)

    fun loadContact(data: AddEditScreenData) {
        val nameParts = data.contactList.fullName.split(" ")
        firstName.value = TextFieldValue(nameParts.firstOrNull() ?: "")
        sureName.value = TextFieldValue(nameParts.getOrNull(1) ?: "")
        company.value = TextFieldValue(data.contactList.course)
        phone.value = TextFieldValue(data.contactList.phone)
        profileImage.value = data.contactList.profileImage
    }

    fun updateProfileImage(imagePath: String?) {
        profileImage.value = imagePath
    }

    fun saveSingleContact(context: Context, user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = Utilities.saveContact(context, user)
            withContext(Dispatchers.Main) {
                onResult(success)
            }
        }
    }

    fun getUpdatedUser(originalUser: User): User {
        return User(
            id = originalUser.id.toString(),
            fullName = "${firstName.value.text} ${sureName.value.text}",
            email = originalUser.email,
            phone = phone.value.text,
            course = company.value.text,
            profileImage = profileImage.value
        )
    }



    fun saveContact(
        originalUser: User?,
        onSaveComplete: (User) -> Unit
    ) {

        if (originalUser == null) return
        val updatedUser = User(
            id = originalUser.id.toString(),
            fullName = "${firstName.value.text} ${sureName.value.text}",
            email = originalUser.email,
            phone = phone.value.text,
            course = company.value.text,
            profileImage = profileImage.value
        )
        onSaveComplete(updatedUser)
    }
}