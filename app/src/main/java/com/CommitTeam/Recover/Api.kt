package com.CommitTeam.Recover

import com.CommitTeam.Recover.models.*
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("users/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfile>

    @PATCH("users/update")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body request: UpdateProfileRequest): Response<Void>

    @GET("users/search")
    suspend fun searchUsers(@Header("Authorization") token: String, @Query("query") query: String): Response<List<UserProfile>>

    @GET("users/{id}")
    suspend fun getUserById(@Header("Authorization") token: String, @Path("id") userId: String): Response<UserProfile>

    @POST("chats/create")
    suspend fun createChat(@Header("Authorization") token: String, @Body request: CreateChatRequest): Response<CreateChatResponse>

    @GET("chats")
    suspend fun getChats(@Header("Authorization") token: String): Response<List<Chat>>

    @GET("chats/{chatId}/messages")
    suspend fun getChatMessages(@Header("Authorization") token: String, @Path("chatId") chatId: String): Response<List<Message>>
}