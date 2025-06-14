package com.cubex.contactsapp.screens.contacts_screen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cubex.contactsapp.screens.contacts_screen.ContactManager
import com.cubex.contactsapp.screens.contacts_screen.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val error: String? = null
)

class ContactsViewModel(private val contactManager: ContactManager) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList()) // Store all contacts
    private val _searchQuery = MutableStateFlow("")
    private val _hasPermission = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _showPermissionRationale = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    init {
        // Combine all state flows to create the UI state
        viewModelScope.launch {
            combine(
                _allContacts,
                _searchQuery,
                _hasPermission,
                _isLoading,
                _showPermissionRationale,
                _error
            ) { values: Array<Any?> ->
                val allContacts = values[0] as List<Contact>
                val searchQuery = values[1] as String
                val hasPermission = values[2] as Boolean
                val isLoading = values[3] as Boolean
                val showPermissionRationale = values[4] as Boolean
                val error = values[5] as String?

                val filteredContacts = if (searchQuery.isEmpty()) {
                    allContacts
                } else {
                    allContacts.filter { contact ->
                        contact.name.contains(searchQuery, ignoreCase = true) ||
                                contact.number.contains(searchQuery)
                    }
                }

                ContactsUiState(
                    contacts = allContacts,
                    filteredContacts = filteredContacts,
                    searchQuery = searchQuery,
                    hasPermission = hasPermission,
                    isLoading = isLoading,
                    showPermissionRationale = showPermissionRationale,
                    error = error
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun checkPermission() {
        _hasPermission.value = contactManager.hasReadPermission()
        if (_hasPermission.value) {
            loadContacts()
        }
    }

    fun onPermissionGranted() {
        _hasPermission.value = true
        _showPermissionRationale.value = false
        loadContacts()
    }

    fun onPermissionDenied() {
        _hasPermission.value = false
        _showPermissionRationale.value = true
        _isLoading.value = false
    }

    fun onRequestPermission() {
        _showPermissionRationale.value = false
        _isLoading.value = true
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // No need for separate performSearch - filtering happens in combine block
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val loadedContacts = contactManager.getAllContacts()
                _allContacts.value = loadedContacts
            } catch (e: Exception) {
                _error.value = "Failed to load contacts: ${e.message}"
                _allContacts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncContacts() {
        // Reset search query when syncing to show all contacts
        _searchQuery.value = ""
        loadContacts()
    }

    // Method to refresh contacts (useful for onResume)
    fun refreshContacts() {
        if (_hasPermission.value) {
            loadContacts()
        }
    }

    fun onAddContact() {
        // Implementation for adding contact
    }

    fun clearError() {
        _error.value = null
    }
}

class ContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            return ContactsViewModel(ContactManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}