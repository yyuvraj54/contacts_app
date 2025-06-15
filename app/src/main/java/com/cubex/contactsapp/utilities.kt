package com.cubex.contactsapp

import android.content.ContentProviderOperation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.cubex.contactsapp.retrofit.model.User
import java.io.ByteArrayOutputStream
import java.io.File

object Utilities {

    fun saveContact(context: Context, user: User): Boolean {
        return try {
            val operations = ArrayList<ContentProviderOperation>()

            // Create a new raw contact
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Add display name
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, user.fullName)
                    .build()
            )

            // Add phone number
            if (user.phone.isNotEmpty()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, user.phone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build()
                )
            }

            // Add email
            if (user.email.isNotEmpty()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, user.email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                        .build()
                )
            }

            // Add organization (course as company)
            if (user.course.isNotEmpty()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, user.course)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                        .build()
                )
            }

            // Add profile photo if available
            user.profileImage?.let { imagePath ->
                val photoBytes = getImageBytes(context, imagePath)
                if (photoBytes != null) {
                    operations.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                            .build()
                    )
                }
            }

            // Execute batch operation
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            Log.d("ContactSave", "Successfully saved contact: ${user.fullName} ${results.toString()}")
            true

        } catch (e: Exception) {
            Log.e("ContactSave", "Error saving contact: ${user.fullName}", e)
            false
        }
    }


    private fun getImageBytes(imagePath: String): ByteArray? {
        return try {
            val bitmap = when {
                imagePath.startsWith("content://") -> {
                    // Handle content URI
                    BitmapFactory.decodeStream(
                        // Note: You'll need to pass context here for content resolver
                        // This is a simplified version
                        null
                    )
                }
                imagePath.startsWith("/") -> {
                    // Handle file path
                    val file = File(imagePath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(imagePath)
                    } else null
                }
                else -> null
            }

            bitmap?.let { bmp ->
                val stream = ByteArrayOutputStream()
                // Compress to reduce size
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()
                stream.close()
                bmp.recycle()
                byteArray
            }
        } catch (e: Exception) {
            Log.e("ContactSave", "Error processing image: $imagePath", e)
            null
        }
    }


    private fun getImageBytes(context: Context, imagePath: String): ByteArray? {
        return try {
            val bitmap = when {
                imagePath.startsWith("content://") -> {
                    // Handle content URI
                    val uri = Uri.parse(imagePath)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                imagePath.startsWith("/") -> {
                    // Handle file path
                    val file = File(imagePath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(imagePath)
                    } else null
                }
                else -> null
            }

            bitmap?.let { bmp ->
                val stream = ByteArrayOutputStream()
                // Compress to reduce size
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()
                stream.close()
                bmp.recycle()
                byteArray
            }
        } catch (e: Exception) {
            Log.e("ContactSave", "Error processing image: $imagePath", e)
            null
        }
    }

    fun contactExists(context: Context, phoneNumber: String): Boolean {
        return try {
            val normalizedPhone = phoneNumber.replace(Regex("[^\\d]"), "")
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null,
                null,
                null
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val existingPhone = it.getString(0).replace(Regex("[^\\d]"), "")
                    if (existingPhone.endsWith(normalizedPhone.takeLast(10)) ||
                        normalizedPhone.endsWith(existingPhone.takeLast(10))) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.e("ContactSave", "Error checking contact existence", e)
            false
        }
    }


    fun saveContactsWithDuplicateCheck(
        context: Context,
        users: List<User>,
        skipDuplicates: Boolean = true
    ): Triple<Int, Int, Int> {
        var successCount = 0
        var failureCount = 0
        var skippedCount = 0

        users.forEach { user ->
            when {
                skipDuplicates && contactExists(context, user.phone) -> {
                    skippedCount++
                    Log.d("ContactSave", "Skipped duplicate contact: ${user.fullName}")
                }
                saveContact(context, user) -> {
                    successCount++
                }
                else -> {
                    failureCount++
                }
            }
        }

        Log.d("ContactSave", "Batch save with duplicate check completed. Success: $successCount, Failed: $failureCount, Skipped: $skippedCount")
        return Triple(successCount, failureCount, skippedCount)
    }


    internal fun getRealPathFromURI(context: Context, uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
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