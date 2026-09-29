package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val fullName: String,
    val title: String,
    val email: String,
    val phone: String,
    val location: String,
    val bio: String,
    val educationsJson: String = "[]",
    val experiencesJson: String = "[]",
    val skillsJson: String = "[]",
    val certificationsJson: String = "[]",
    val jobPreferencesJson: String = "{}",
    val cvJson: String? = null
)
