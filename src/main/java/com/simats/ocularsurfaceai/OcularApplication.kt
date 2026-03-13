package com.simats.ocularsurfaceai

import android.app.Application
import com.simats.ocularsurfaceai.data.SessionManager

class OcularApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
    }

    companion object {
        var sessionManager: SessionManager? = null
            private set
    }
}
