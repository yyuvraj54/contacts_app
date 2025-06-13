package com.cubex.contactsapp.screens.contacts_screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.cubex.contactsapp.screens.contacts_screen.model.Contact
import com.cubex.contactsapp.screens.contacts_screen.model.ContactDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data classes for contact information


// Contact Manager Class
class ContactManager(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    companion object {
        const val PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS
        const val PERMISSION_WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
    }

    // Permission checks
    fun hasReadPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }



    suspend fun getAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        if (!hasReadPermission()) return@withContext emptyList()

        val contacts = mutableListOf<Contact>()
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val contactId = it.getString(idIndex) ?: ""
                val name = it.getString(nameIndex) ?: "Unknown"
                val phoneNumber = it.getString(phoneIndex) ?: ""
                val photoUri = it.getString(photoIndex) ?: ""

                contacts.add(Contact(contactId, name, phoneNumber, "", photoUri))
            }
        }

        return@withContext contacts.distinctBy { it.id }
    }

    // Get contacts with names only (no phone numbers required)
    suspend fun getContactsNamesOnly(): List<Contact> = withContext(Dispatchers.IO) {
        if (!hasReadPermission()) return@withContext emptyList()

        val contacts = mutableListOf<Contact>()
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

            while (it.moveToNext()) {
                val contactId = it.getString(idIndex) ?: ""
                val name = it.getString(nameIndex) ?: "Unknown"
                val photoUri = it.getString(photoIndex) ?: ""

                contacts.add(Contact(contactId, name, "", "", photoUri))
            }
        }

        return@withContext contacts
    }

    suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        if (!hasReadPermission() || query.isEmpty()) return@withContext emptyList()

        val contacts = mutableListOf<Contact>()
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val contactId = it.getString(idIndex) ?: ""
                val name = it.getString(nameIndex) ?: "Unknown"
                val phoneNumber = it.getString(phoneIndex) ?: ""
                val photoUri = it.getString(photoIndex) ?: ""

                contacts.add(Contact(contactId, name, phoneNumber, "", photoUri))
            }
        }

        return@withContext contacts.distinctBy { it.id }
    }


    @SuppressLint("Range")
    suspend fun getContactDetails(contactId: String): ContactDetails? = withContext(Dispatchers.IO) {
        if (!hasReadPermission()) return@withContext null

        var name = ""
        var photoUri = ""
        var organization = ""
        var note = ""
        val phoneNumbers = mutableListOf<String>()
        val emails = mutableListOf<String>()

        // Get basic contact info
        val contactCursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId),
            null
        )

        contactCursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
                photoUri = it.getString(it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) ?: ""
            }
        }

        // Get phone numbers
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use {
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val phone = it.getString(phoneIndex)
                if (!phone.isNullOrEmpty()) {
                    phoneNumbers.add(phone)
                }
            }
        }

        // Get emails
        val emailCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        emailCursor?.use {
            val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (it.moveToNext()) {
                val email = it.getString(emailIndex)
                if (!email.isNullOrEmpty()) {
                    emails.add(email)
                }
            }
        }

        // Get organization
        val orgCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
            null
        )

        orgCursor?.use {
            if (it.moveToFirst()) {
                organization = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)) ?: ""
            }
        }

        // Get note
        val noteCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Note.NOTE),
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE),
            null
        )

        noteCursor?.use {
            if (it.moveToFirst()) {
                note = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)) ?: ""
            }
        }

        return@withContext ContactDetails(
            id = contactId,
            name = name,
            phoneNumbers = phoneNumbers,
            emails = emails,
            photoUri = photoUri,
            organization = organization,
            note = note
        )
    }
}