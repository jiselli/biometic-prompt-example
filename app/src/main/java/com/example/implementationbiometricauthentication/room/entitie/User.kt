package com.example.implementationbiometricauthentication.room.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val email: String,
    val password: String
)