package com.CommitTeam.Recover.models

data class UserProfile(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val dob: Long? = null,          // ✅ дата рождения (timestamp или null)
    val lastSeen: Long? = null      // ✅ время последнего онлайна
)