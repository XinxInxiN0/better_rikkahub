package com.u4e50.rikkahub.data.repository

import androidx.paging.PagingSource
import com.u4e50.rikkahub.data.db.dao.GenMediaDAO
import com.u4e50.rikkahub.data.db.entity.GenMediaEntity

class GenMediaRepository(private val dao: GenMediaDAO) {
    fun getAllMedia(): PagingSource<Int, GenMediaEntity> = dao.getAll()

    suspend fun insertMedia(media: GenMediaEntity) = dao.insert(media)

    suspend fun deleteMedia(id: Int) = dao.delete(id)
}

