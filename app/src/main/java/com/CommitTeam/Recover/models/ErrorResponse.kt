package com.CommitTeam.Recover.models

import com.google.gson.annotations.SerializedName

/**
 * Data-класс для безопасного парсинга ЛЮБОГО ответа об ошибке от сервера.
 */
data class ErrorResponse(
    @SerializedName("error")
    val error: String?,

    @SerializedName("message")
    val message: String?,

    // 1. ИСПРАВЛЕНО: Переименовано, чтобы избежать конфликта
    @SerializedName("banInfo")
    val banInfo: ErrorBanInfo?
)

/**
 * Вложенный класс для BanInfo.
 * Переименован в ErrorBanInfo, чтобы не конфликтовать с BanInfo из AuthResponse.kt
 */
data class ErrorBanInfo(
    // 2. ИСПРАВЛЕНО: Добавлен '?' для фикса предупреждений
    @SerializedName("reason")
    val reason: String?,

    @SerializedName("bannedAt")
    val bannedAt: Double?,

    // 3. ИСПРАВЛЕНО: Добавлен '?' для фикса предупреждений
    @SerializedName("banUntil")
    val banUntil: Double?
)