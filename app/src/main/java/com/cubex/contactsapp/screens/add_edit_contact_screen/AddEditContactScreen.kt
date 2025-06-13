package com.cubex.contactsapp.screens.add_edit_contact_screen

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cubex.contactsapp.R
import com.cubex.contactsapp.ui.theme.profileIconBackgroundColor
import com.cubex.contactsapp.ui.theme.saveIconButtonBottomColor
import com.cubex.contactsapp.ui.theme.saveIconButtonTopColor
import com.cubex.contactsapp.ui.theme.screenBackgroundBottomColor
import com.cubex.contactsapp.ui.theme.screenBackgroundTopColor



@Composable
@Preview
fun AddEditContactScreen(
    isEditMode: Boolean = false,
    viewModel: ContactViewModel = viewModel(),
    screenData:AddEditScreenData? =null
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
        TopBar(
            title = if (isEditMode) "Edit Contact" else "Add Contact",
            onSaveClick = { viewModel.saveContact(isEditMode)},
            onMenuClick = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PictureSelector(onClick = { })

        Spacer(modifier = Modifier.height(24.dp))

        ContactTextField(
            label = "First Name",
            value = viewModel.firstName,
            onValueChange = { viewModel.firstName = it }
        )

        ContactTextField(
            label = "Sure Name",
            value = viewModel.sureName,
            onValueChange = { viewModel.sureName = it }
        )

        ContactTextField(
            label = "Company",
            value = viewModel.company,
            onValueChange = { viewModel.company = it }
        )

        ContactTextField(
            label = "Phone",
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
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

@Composable
fun PictureSelector(onClick: () -> Unit) {
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
            Icon(
                painterResource(id = R.drawable.gallery_icon),
                contentDescription = "Add Picture",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Add Picture")
    }
}

@Composable
fun ContactTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        label = { Text(text = label) }
    )
}

class ContactViewModel : ViewModel() {
    var firstName by mutableStateOf(TextFieldValue(""))
    var sureName by mutableStateOf(TextFieldValue(""))
    var company by mutableStateOf(TextFieldValue(""))
    var phone by mutableStateOf(TextFieldValue(""))

    fun saveContact(isEditMode:Boolean) {
        if(isEditMode == true) {

        }
    }
}
