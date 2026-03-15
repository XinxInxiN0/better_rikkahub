package com.u4e50.rikkahub.di

import com.u4e50.rikkahub.ui.pages.assistant.AssistantVM
import com.u4e50.rikkahub.ui.pages.assistant.detail.AssistantDetailVM
import com.u4e50.rikkahub.ui.pages.backup.BackupVM
import com.u4e50.rikkahub.ui.pages.chat.ChatVM
import com.u4e50.rikkahub.ui.pages.debug.DebugVM
import com.u4e50.rikkahub.ui.pages.developer.DeveloperVM
import com.u4e50.rikkahub.ui.pages.extensions.QuickMessagesVM
import com.u4e50.rikkahub.ui.pages.extensions.SkillsVM
import com.u4e50.rikkahub.ui.pages.favorite.FavoriteVM
import com.u4e50.rikkahub.ui.pages.history.HistoryVM
import com.u4e50.rikkahub.ui.pages.imggen.ImgGenVM
import com.u4e50.rikkahub.ui.pages.prompts.PromptVM
import com.u4e50.rikkahub.ui.pages.search.SearchVM
import com.u4e50.rikkahub.ui.pages.setting.SettingVM
import com.u4e50.rikkahub.ui.pages.share.handler.ShareHandlerVM
import com.u4e50.rikkahub.ui.pages.stats.StatsVM
import com.u4e50.rikkahub.ui.pages.translator.TranslatorVM
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<ChatVM> { params ->
        ChatVM(
            id = params.get(),
            context = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatService = get(),
            updateChecker = get(),
            filesManager = get(),
            favoriteRepository = get(),
        )
    }
    viewModelOf(::SettingVM)
    viewModelOf(::DebugVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::AssistantVM)
    viewModel<AssistantDetailVM> {
        AssistantDetailVM(
            id = it.get(),
            settingsStore = get(),
            memoryRepository = get(),
            filesManager = get(),
            skillManager = get(),
        )
    }
    viewModelOf(::TranslatorVM)
    viewModel<ShareHandlerVM> {
        ShareHandlerVM(
            text = it.get(),
            settingsStore = get(),
        )
    }
    viewModelOf(::BackupVM)
    viewModelOf(::ImgGenVM)
    viewModelOf(::DeveloperVM)
    viewModelOf(::PromptVM)
    viewModelOf(::QuickMessagesVM)
    viewModelOf(::SkillsVM)
    viewModelOf(::FavoriteVM)
    viewModelOf(::SearchVM)
    viewModelOf(::StatsVM)
}
