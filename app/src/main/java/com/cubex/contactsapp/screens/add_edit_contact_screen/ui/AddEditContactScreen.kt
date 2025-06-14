package com.cubex.contactsapp.screens.add_edit_contact_screen.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cubex.contactsapp.R
import com.cubex.contactsapp.components.CircularPopup
import com.cubex.contactsapp.components.GradientCircularProgressBar
import com.cubex.contactsapp.retrofit.model.User
import com.cubex.contactsapp.screens.add_edit_contact_screen.model.AddEditScreenData
import com.cubex.contactsapp.screens.add_edit_contact_screen.viewmodel.ContactFormViewModel
import com.cubex.contactsapp.ui.theme.profileIconBackgroundColor
import com.cubex.contactsapp.ui.theme.saveIconButtonBottomColor
import com.cubex.contactsapp.ui.theme.saveIconButtonTopColor
import com.cubex.contactsapp.ui.theme.screenBackgroundBottomColor
import com.cubex.contactsapp.ui.theme.screenBackgroundTopColor
import kotlin.math.sin


@Composable
fun AddEditContactScreen(
    isEditMode: Boolean = false,
    viewModel: ContactFormViewModel = viewModel(),
    screenData: AddEditScreenData? = null,
    onImagePickerClick: () -> Unit = {},
    onSaveAndExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        screenBackgroundTopColor, screenBackgroundBottomColor
                    )
                )
            )
    ) {
        val context = LocalContext.current
        var showSuccessDialog by remember { mutableStateOf(false) }
        TopBar(
            title = screenData?.screenTitle ?: "Add Contact",
            onSaveClick = {
                if (!isEditMode) {
                    val newUser = User(
                        fullName = "${viewModel.firstName.value.text} ${viewModel.sureName.value.text}",
                        phone = viewModel.phone.value.text,
                        course = viewModel.company.value.text,
                        profileImage = viewModel.profileImage.value,
                        id = "",
                        email = ""
                    )
                    viewModel.saveSingleContact(context, newUser) { success ->
                        if (success) {
                            showSuccessDialog = true
                        }
                    }
                } else {
                    viewModel.saveContact(
                        originalUser = screenData?.contactList
                    ) { updatedUser ->
                        val resultIntent = Intent().apply {
                            putExtra("updatedUser", updatedUser)
                        }
                        (context as? Activity)?.setResult(Activity.RESULT_OK, resultIntent)
                        (context as? Activity)?.finish()
                        onSaveAndExit()
                    }
                }
            },
            onMenuClick = { }
        )
        if (showSuccessDialog) {
            Dialog(onDismissRequest = {
                showSuccessDialog = false
                (context as? Activity)?.finish()
                onSaveAndExit()
            }) {
                CircularPopup(
                    label = "Contact added\nSuccessfully",
                    onDismiss = {
                        showSuccessDialog = false
                        (context as? Activity)?.finish()
                        onSaveAndExit()
                    }
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        PictureSelector(
            currentImagePath = viewModel.profileImage.value,
            onClick = onImagePickerClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        ContactTextField(
            label = "First Name",
            value = viewModel.firstName.value,
            onValueChange = { viewModel.firstName.value = it }
        )

        ContactTextField(
            label = "Sure Name",
            value = viewModel.sureName.value,
            onValueChange = { viewModel.sureName.value = it }
        )

        ContactTextField(
            label = "Company",
            value = viewModel.company.value,
            onValueChange = { viewModel.company.value = it }
        )

        ContactTextField(
            label = "Phone",
            value = viewModel.phone.value,
            onValueChange = { viewModel.phone.value = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, onSaveClick: () -> Unit, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        actions = {
            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(8.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(saveIconButtonTopColor, saveIconButtonBottomColor)
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                Text("    Save    ")
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        },
        navigationIcon = {
            Spacer(modifier = Modifier.width(16.dp))
        }
    )
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun PictureSelector(
    currentImagePath: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(profileIconBackgroundColor, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (currentImagePath != null) {
                // Display selected image using Glide
                GlideImage(
                    model = currentImagePath,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                ) {
                    it.error(R.drawable.gallery_icon)
                        .placeholder(R.drawable.gallery_icon)
                }
            } else {
                // Display default gallery icon
                Icon(
                    painterResource(id = R.drawable.gallery_icon),
                    contentDescription = "Add Picture",
                    tint = Color.Black,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = if (currentImagePath != null) "Change Picture" else "Add Picture")
    }
}

@Composable
fun ContactTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Label positioned above the input field
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0x4D000000),
            modifier = Modifier.padding(bottom = 4.dp, start = 24.dp)
        )

        // Input field with custom styling
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                .height(56.dp),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0x4D000000), // #0000004D
                focusedBorderColor = Color(0x4D000000),
                unfocusedTextColor = Color(0x4D000000),
                focusedTextColor = Color(0x4D000000)
            )
        )
    }
}