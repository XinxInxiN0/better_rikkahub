package com.u4e50.rikkahub.ui.context

import androidx.compose.runtime.compositionLocalOf
import com.u4e50.rikkahub.ui.hooks.CustomTtsState

val LocalTTSState = compositionLocalOf<CustomTtsState> { error("Not provided yet") }

