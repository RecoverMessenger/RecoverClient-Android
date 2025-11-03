// AuthResponse.kt
package com.CommitTeam.Recover

data class AuthResponse(
    val message: String? = null,
    val token: String? = null,
    val userId: String? = null,
    val error: String? = null,
    val banInfo: BanInfo? = null
)

data class BanInfo(
    val reason: String,
    val bannedAt: Long,
    val banUntil: Long
)