package com.cubex.contactsapp.model


data class UserResponse(
    val success: Boolean,
    val Data: UserData
)

data class UserData(
    val date: String,
    val totalUsers: Int,
    val users: List<User>
)
