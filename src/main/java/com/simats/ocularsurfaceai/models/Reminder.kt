package com.simats.ocularsurfaceai.models

data class Reminder(
    val id: Long,
    val medicineName: String,
    val hour: Int,
    val minute: Int,
    val isActive: Boolean = true
)
