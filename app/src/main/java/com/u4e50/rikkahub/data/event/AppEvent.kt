package com.u4e50.rikkahub.data.event

sealed class AppEvent {
    data class Speak(val text: String) : AppEvent()
}

