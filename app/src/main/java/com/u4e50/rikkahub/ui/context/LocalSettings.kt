package com.u4e50.rikkahub.ui.context

import androidx.compose.runtime.staticCompositionLocalOf
import com.u4e50.rikkahub.data.datastore.Settings

val LocalSettings = staticCompositionLocalOf<Settings> {
    error("No SettingsStore provided")
}

