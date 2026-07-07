package com.haoze.claudekeyboard

import android.app.Application
import com.google.android.material.color.DynamicColors

class SyncTouchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
