package com.CommitTeam.Recover.Database

import com.CommitTeam.Recover.models.Message


// Убрали аннотации Room (@Entity, @PrimaryKey), оставили простой data class
data class MessageEntity(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val status: String?
)

// Конвертер из сетевой модели в локальную (если всё ещё используется)
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = this.id,
        chatId = this.chatId,
        senderId = this.senderId,
        content = this.content,
        timestamp = this.timestamp,
        status = this.status
    )
}

// Конвертер обратно (если где-то нужно)
fun MessageEntity.toDomain(): Message {
    return Message(
        id = this.id,
        chatId = this.chatId,
        senderId = this.senderId,
        content = this.content,
        timestamp = this.timestamp,
        status = this.status
    )
}
