package com.CommitTeam.Recover.models

data class UpdateProfileRequest(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val dob: Long? = null
)