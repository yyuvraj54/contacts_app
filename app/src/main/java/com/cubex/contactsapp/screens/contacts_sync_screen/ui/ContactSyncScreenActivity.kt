package com.cubex.contactsapp.screens.contacts_sync_screen.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cubex.contactsapp.retrofit.model.User
import com.cubex.contactsapp.ui.theme.ContactsAppTheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cubex.contactsapp.components.CircularPopup
import com.cubex.contactsapp.components.GradientCircularProgressBar
import com.cubex.contactsapp.screens.add_edit_contact_screen.ui.AddEditContactActivity
import com.cubex.contactsapp.screens.contacts_sync_screen.viewmodel.ContactSyncViewModel
import com.cubex.contactsapp.ui.theme.screenBackgroundBottomColor
import com.cubex.contactsapp.ui.theme.screenBackgroundTopColor
import com.google.gson.Gson
import kotlin.jvm.java

class ContactSyncScreenActivity : ComponentActivity() {
    private lateinit var viewModel: ContactSyncViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ContactSyncViewModel::class.java]

        // FIXED: Use Activity Result API properly
        val editContactLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

                // FIXED: Get the User object directly (not JSON string)
                val updatedUser = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra("updatedUser", User::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getSerializableExtra("updatedUser") as? User
                }

                updatedUser?.let {
                    Log.d("ContactSync", "Received updated user: $it")
                    viewModel.updateContact(it)
                } ?: Log.e("ContactSync", "User data was null")

        }



        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
        } else {
            viewModel.fetchAndFilterContacts(this)
        }

        setContent {
            ContactsAppTheme {
                val contactsViewModel: ContactSyncViewModel = viewModel()

                val usersState = contactsViewModel.contacts.collectAsState()
                val syncDateState = contactsViewModel.syncDate.collectAsState()
                val isLoadingState = contactsViewModel.loading.collectAsState()

                val context = LocalContext.current

                ContactSyncScreen(
                    viewModel = viewModel,
                    users = usersState.value,
                    syncDate = syncDateState.value,
                    isLoading = isLoadingState.value,
                    onSyncClick = { contactsViewModel.fetchAndFilterContacts(context) },
                    onEditClick = { user ->
                        val intent = Intent(context, AddEditContactActivity::class.java)
                        val userJson = Gson().toJson(user)
                        intent.putExtra("user_data", userJson)
                        intent.putExtra("is_edit_mode", true)

                        editContactLauncher.launch(intent)
                    },
                )
            }
        }
    }
}
@Composable
fun ContactSyncScreen(

    viewModel: ContactSyncViewModel,
    users: List<User>,
    syncDate: String,
    isLoading: Boolean = false,
    onSyncClick: () -> Unit = {},
    onEditClick: (User) -> Unit

) {
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            screenBackgroundTopColor,
            screenBackgroundBottomColor
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            TopBar(title = "New contacts found")

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        GradientCircularProgressBar()
                    }
                }

                users.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No new contacts found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(users) { index, user ->
                            val gradient = cardGradients[index % cardGradients.size]
                            ContactCard(user = user, gradient = gradient,onEditClick)
                        }
                    }
                }
            }
            BottomBar(syncDate = syncDate, viewModel = viewModel, users = users)
        }
    }
}
@Composable
fun TopBar(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun ContactCard(user: User, gradient: Brush,  onEditClick:(User) -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(brush = gradient)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = user.course,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.clickable {
                            onEditClick(user)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.phone,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.email,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun BottomBar(
    syncDate: String,
    viewModel: ContactSyncViewModel,
    users: List<User>
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var syncResult by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Synced", style = MaterialTheme.typography.bodySmall)
                Text(syncDate, style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = {
                viewModel.saveContactsWithDuplicateCheck(context, users) { success, failed, skipped ->
                    syncResult = "Saved: $success, Failed: $failed, Skipped: $skipped"
                    showPopup = true
                }
            }) {
                Text("Sync Contacts")
            }
        }

        if (syncResult.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(syncResult, style = MaterialTheme.typography.bodySmall)
        }
    }

    // ✅ Show popup as dialog
    if (showPopup) {
        Dialog(onDismissRequest = { }) {
            CircularPopup(
                label = "Contacts Synced\nSuccessfully",
                onDismiss = {
                    showPopup = false
                    activity?.finish()
                }
            )
        }
    }
}

val cardGradients = listOf(
    Brush.verticalGradient(colors = listOf(Color(0xFFff7e5f), Color(0xFFfeb47b))),
    Brush.verticalGradient(colors = listOf(Color(0xFF6a11cb), Color(0xFF2575fc))),
    Brush.verticalGradient(colors = listOf(Color(0xFF43cea2), Color(0xFF185a9d))),
    Brush.verticalGradient(colors = listOf(Color(0xFFff512f), Color(0xFFdd2476)))
)



