package com.CommitTeam.Recover.models

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val status: String? = null // одно поле со значением по умолчанию
)
