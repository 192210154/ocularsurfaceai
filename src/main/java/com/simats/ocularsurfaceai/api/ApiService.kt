package com.simats.ocularsurfaceai.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class AuthResponse(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val gender: String?,
    val qualification: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val gender: String,
    val qualification: String
)

data class HistoryItem(
    val id: String,
    val disease: String,
    val confidence: Int,
    val severity: String,
    val image_url: String,
    val date: String,
    val time: String
)

data class SaveResultData(
    val id: String,
    val image_path: String
)

data class PredictResponse(
    val disease: String,
    val confidence: Float,
    val confidence_percent: Int,
    val severity: String,
    val history_id: Int,
    val image_path: String
)

// --- NEW DATA MODEL ---
data class SimpleResponse(
    val error: Boolean,
    val message: String
)

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @Headers("Content-Type: application/json")
    @POST("auth/register")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @Multipart
    @POST("predict")
    suspend fun saveResult(
        @Part image: MultipartBody.Part
    ): PredictResponse

    @GET("history")
    suspend fun getHistory(): List<HistoryItem>

    // --- NEW ENDPOINTS ---
    @FormUrlEncoded
    @POST("auth/change_password")
    suspend fun changePassword(
        @Field("email") email: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String
    ): Response<SimpleResponse>

    @FormUrlEncoded
    @POST("auth/delete_account")
    suspend fun deleteAccount(
        @Field("email") email: String
    ): Response<SimpleResponse>

    @FormUrlEncoded
    @POST("auth/forgot_password")
    suspend fun forgotPassword(
        @Field("email") email: String
    ): Response<SimpleResponse>

    @FormUrlEncoded
    @POST("auth/update_profile")
    suspend fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): SimpleResponse

    @FormUrlEncoded
    @POST("auth/verify_otp")
    suspend fun verifyOtp(
        @Field("email") email: String,
        @Field("otp") otp: String
    ): Response<com.simats.ocularsurfaceai.data.VerifyOtpResponse>

    @FormUrlEncoded
    @POST("auth/reset_password")
    suspend fun resetPassword(
        @Field("token") token: String,
        @Field("new_password") newPassword: String
    ): Response<SimpleResponse>
}