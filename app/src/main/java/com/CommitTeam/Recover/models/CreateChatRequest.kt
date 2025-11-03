package com.CommitTeam.Recover.models

data class CreateChatRequest(
    val members: List<String>,
    val name: String? = null
)