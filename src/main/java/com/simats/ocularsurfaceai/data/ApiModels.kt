package com.simats.ocularsurfaceai.data

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

// --- NEW DATA MODEL ---
data class SimpleResponse(
    val error: Boolean,
    val message: String
)

data class VerifyOtpResponse(
    val error: Boolean,
    val message: String,
    val reset_token: String? = null
)

data class PredictResponse(
    val disease: String,
    val confidence: Float,
    val severity: String? = null
)