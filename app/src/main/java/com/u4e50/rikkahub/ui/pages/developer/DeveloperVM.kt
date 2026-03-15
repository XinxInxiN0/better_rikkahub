package com.u4e50.rikkahub.ui.pages.developer

import androidx.lifecycle.ViewModel
import com.u4e50.rikkahub.data.ai.AILoggingManager

class DeveloperVM(
    private val aiLoggingManager: AILoggingManager
) : ViewModel() {
    val logs = aiLoggingManager.getLogs()
}

