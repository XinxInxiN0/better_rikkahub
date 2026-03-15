package com.u4e50.rikkahub.di

import com.u4e50.rikkahub.data.files.FilesManager
import com.u4e50.rikkahub.data.repository.ConversationRepository
import com.u4e50.rikkahub.data.repository.FavoriteRepository
import com.u4e50.rikkahub.data.repository.FilesRepository
import com.u4e50.rikkahub.data.repository.GenMediaRepository
import com.u4e50.rikkahub.data.repository.MemoryRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        ConversationRepository(get(), get(), get(), get(), get(), get())
    }

    single {
        MemoryRepository(get())
    }

    single {
        GenMediaRepository(get())
    }

    single {
        FilesRepository(get())
    }

    single {
        FavoriteRepository(get())
    }

    single {
        FilesManager(get(), get(), get())
    }
}

