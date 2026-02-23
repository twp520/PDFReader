package com.ncw6fg.nxhw18e.pdfreader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * create by colin
 * 2026/2/19
 */

@Entity(tableName = "file_metadata")
data class FileMetadata(
    val id: Long,
    @PrimaryKey
    val path: String,
    val isFavorite: Boolean
)