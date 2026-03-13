package com.simats.ocularsurfaceai.utils

import android.content.Context

object Prefs {
    private const val PREF_NAME = "ocular_prefs"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    fun isOnboardingDone(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingDone(context: Context, done: Boolean = true) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_DONE, done)
            .apply()
    }
}