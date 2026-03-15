package com.u4e50.rikkahub.di

import kotlinx.serialization.json.Json
import me.rerere.highlight.Highlighter
import com.u4e50.rikkahub.AppScope
import com.u4e50.rikkahub.data.ai.AILoggingManager
import com.u4e50.rikkahub.data.ai.tools.LocalTools
import com.u4e50.rikkahub.data.event.AppEventBus
import com.u4e50.rikkahub.service.ChatService
import com.u4e50.rikkahub.utils.EmojiData
import com.u4e50.rikkahub.utils.EmojiUtils
import com.u4e50.rikkahub.utils.JsonInstant
import com.u4e50.rikkahub.utils.UpdateChecker
import com.u4e50.rikkahub.web.WebServerManager
import me.rerere.tts.provider.TTSManager
import org.koin.dsl.module

val appModule = module {
    single<Json> { JsonInstant }

    single {
        Highlighter(get())
    }

    single {
        AppEventBus()
    }

    single {
        LocalTools(get(), get())
    }

    single {
        UpdateChecker(get())
    }

    single {
        AppScope()
    }

    single<EmojiData> {
        EmojiUtils.loadEmoji(get())
    }

    single {
        TTSManager(get())
    }

    single {
        AILoggingManager()
    }

    single {
        ChatService(
            context = get(),
            appScope = get(),
            settingsStore = get(),
            conversationRepo = get(),
            memoryRepository = get(),
            generationHandler = get(),
            templateTransformer = get(),
            providerManager = get(),
            localTools = get(),
            mcpManager = get(),
            filesManager = get(),
            skillManager = get()
        )
    }

    single {
        WebServerManager(
            context = get(),
            appScope = get(),
            chatService = get(),
            conversationRepo = get(),
            settingsStore = get(),
            filesManager = get()
        )
    }
}

