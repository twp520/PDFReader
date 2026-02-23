package com.ncw6fg.nxhw18e.pdfreader.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ncw6fg.nxhw18e.pdfreader.data.db.FileMetadata
import com.ncw6fg.nxhw18e.pdfreader.data.db.FileMetadataDao

/**
 * create by colin
 * 2026/2/19
 */
@Database(entities = [FileMetadata::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fileMetadataDao(): FileMetadataDao
}