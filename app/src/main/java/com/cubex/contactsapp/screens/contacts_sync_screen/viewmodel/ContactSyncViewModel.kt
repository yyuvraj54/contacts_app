package com.cubex.contactsapp.screens.contacts_sync_screen.viewmodel

import android.content.Context
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cubex.contactsapp.retrofit.RetrofitInstance
import com.cubex.contactsapp.retrofit.model.ContactResponse
import com.cubex.contactsapp.retrofit.model.User

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubex.contactsapp.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactSyncViewModel : ViewModel() {

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts: StateFlow<List<User>> = _contacts

    private val _syncDate = MutableStateFlow<String>("")
    val syncDate: StateFlow<String> = _syncDate

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading





    // Function to save contacts with duplicate checking
    fun saveContactsWithDuplicateCheck(
        context: Context,
        users: List<User>,
        onResult: (Int, Int, Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val (successCount, failureCount, skippedCount) =
                Utilities.saveContactsWithDuplicateCheck(context, users, true)
            withContext(Dispatchers.Main) {
                onResult(successCount, failureCount, skippedCount)
            }
        }
    }

    fun updateContact(updatedUser: User) {
        val current = _contacts.value.toMutableList()
        val index = current.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            current[index] = updatedUser
            _contacts.value = current
        }
    }

    fun fetchAndFilterContacts(context: Context) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response: ContactResponse = RetrofitInstance.api.getContacts()
                if (response.success) {
                    val apiContacts = response.Data.users
                    val deviceNumbers = getDeviceContacts(context)

                    val newContacts = apiContacts.filter { apiUser ->
                        deviceNumbers.none { deviceNumber ->
                            normalizePhone(deviceNumber) == normalizePhone(apiUser.phone)
                        }
                    }

                    _contacts.value = newContacts
                    _syncDate.value = response.Data.date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    private fun getDeviceContacts(context: Context): List<String> {
        val numbers = mutableListOf<String>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                numbers.add(number)
            }
        }

        return numbers
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^\\d]"), "").takeLast(10)
    }


}