package com.u4e50.rikkahub.data.favorite

import com.u4e50.rikkahub.data.db.entity.FavoriteEntity
import com.u4e50.rikkahub.data.model.FavoriteType

interface FavoriteAdapter<T> {
    val type: FavoriteType

    fun buildRefKey(target: T): String

    fun buildFavoriteEntity(
        target: T,
        existing: FavoriteEntity? = null,
        now: Long = System.currentTimeMillis()
    ): FavoriteEntity
}

