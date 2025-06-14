package com.cubex.contactsapp.screens.contacts_screen.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubex.contactsapp.R
import com.cubex.contactsapp.screens.add_edit_contact_screen.ui.AddEditContactActivity


import com.cubex.contactsapp.screens.contacts_screen.model.Contact
import com.cubex.contactsapp.screens.contacts_screen.viewmodel.ContactsUiState
import com.cubex.contactsapp.screens.contacts_screen.viewmodel.ContactsViewModel
import com.cubex.contactsapp.screens.contacts_screen.viewmodel.ContactsViewModelFactory
import com.cubex.contactsapp.screens.contacts_sync_screen.ui.ContactSyncScreenActivity
import com.cubex.contactsapp.ui.theme.ContactsAppTheme
import com.cubex.contactsapp.ui.theme.floatingIconBackgroundColor
import com.cubex.contactsapp.ui.theme.screenBackgroundBottomColor
import com.cubex.contactsapp.ui.theme.screenBackgroundTopColor
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private var viewModel: ContactsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ContactsAppTheme(dynamicColor = false, darkTheme = false) {
                ContactsApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh contacts when returning to the activity
        viewModel?.refreshContacts()
    }

    @Composable
    fun ContactsApp() {
        val context = LocalContext.current
        val contactsViewModel: ContactsViewModel = viewModel(
            factory = ContactsViewModelFactory(context)
        )

        // Store the viewModel reference for onResume
        viewModel = contactsViewModel

        val uiState by contactsViewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // Permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] ?: false

            if (readGranted && writeGranted) {
                contactsViewModel.onPermissionGranted()
            } else {
                contactsViewModel.onPermissionDenied()
            }
        }

        LaunchedEffect(Unit) {
            contactsViewModel.checkPermission()
            if (!uiState.hasPermission) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS
                    )
                )
            }
        }

        // Show error messages in snackbar
        LaunchedEffect(uiState.error) {
            uiState.error?.let { error ->
                snackbarHostState.showSnackbar(error)
                contactsViewModel.clearError()
            }
        }

        Box(
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
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    if (uiState.hasPermission) {
                        ContactFABs(
                            context,
                            onAddContact = contactsViewModel::onAddContact,
                            onSyncContacts = contactsViewModel::syncContacts
                        )
                    }
                }
            ) { padding ->
                when {
                    !uiState.hasPermission && uiState.showPermissionRationale -> {
                        PermissionScreen(
                            onRequestPermission = {
                                contactsViewModel.onRequestPermission()
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }

                    !uiState.hasPermission -> {
                        LoadingScreen(
                            message = "Requesting permissions...",
                            modifier = Modifier.padding(padding)
                        )
                    }

                    uiState.isLoading && uiState.contacts.isEmpty() -> {
                        LoadingScreen(
                            message = "Loading contacts...",
                            modifier = Modifier.padding(padding)
                        )
                    }

                    else -> {
                        ContactScreen(
                            uiState = uiState,
                            onSearchQueryChanged = contactsViewModel::updateSearchQuery,
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PermissionScreen(
        onRequestPermission: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "📱",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "Access Your Contacts",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "To show your contacts, we need permission to access your contact list. This helps you manage and view all your contacts in one place.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }

    @Composable
    fun LoadingScreen(
        message: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun ContactScreen(
        uiState: ContactsUiState,
        onSearchQueryChanged: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier.padding(16.dp)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading indicator if searching
            if (uiState.isLoading && uiState.contacts.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when {
                uiState.filteredContacts.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                    EmptySearchResults()
                }

                uiState.filteredContacts.isEmpty() && uiState.contacts.isEmpty() -> {
                    EmptyContactsList()
                }

                else -> {
                    ContactList(contacts = uiState.filteredContacts)
                }
            }
        }
    }

    @Composable
    fun EmptySearchResults() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔍",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "No contacts found",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Try a different search term",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun EmptyContactsList() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "No contacts found",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Your contact list appears to be empty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun SearchBar(
        query: String,
        onQueryChanged: (String) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .background(color = Color(0xFFE4E4E4), shape = RoundedCornerShape(20.dp))
                .height(50.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = { Text("Search contacts...") },
                leadingIcon = {
                    Icon( painterResource(R.drawable.search_icon), contentDescription = "Search")
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.mic_icon),
                            contentDescription = "Mic Icon"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            painterResource(R.drawable.three_dots_icon),
                            contentDescription = "More Options"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },

                singleLine = true,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }

    @Composable
    fun ContactItem(contact: Contact) {
        val avatarColor = remember {
            Color(
                red = Random.nextFloat(),
                green = Random.nextFloat(),
                blue = Random.nextFloat(),
                alpha = 1f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                // Avatar with first letter
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(avatarColor, shape = CircleShape)
                ) {
                    Text(
                        text = contact.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = contact.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    @Composable
    fun ContactList(contacts: List<Contact>) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(contacts) { contact ->
                ContactItem(contact)
            }
        }
    }

    @Composable
    fun ContactFABs(
        context: Context,
        onAddContact: () -> Unit,
        onSyncContacts: () -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
        ) {

            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, ContactSyncScreenActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = floatingIconBackgroundColor
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sync),
                    contentDescription = "Sync Contacts",
                    tint = Color.White
                )
            }

            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddEditContactActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = floatingIconBackgroundColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = Color.White)
            }
        }
    }
}