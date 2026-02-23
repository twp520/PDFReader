package com.ncw6fg.nxhw18e.pdfreader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * create by colin
 * 2026/2/19
 */
@Dao
interface FileMetadataDao {

    @Query("SELECT * FROM file_metadata")
    fun getAllMetadata(): Flow<List<FileMetadata>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun updateMetadata(metadata: FileMetadata)

    @Query("DELETE FROM file_metadata WHERE path = :path")
    suspend fun deleteMetadata(path: String)

    @Query("SELECT * FROM file_metadata WHERE path = :path")
    suspend fun getMetadata(path: String): FileMetadata?
}