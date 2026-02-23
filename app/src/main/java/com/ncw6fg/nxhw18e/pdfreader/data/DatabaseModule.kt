package com.ncw6fg.nxhw18e.pdfreader.data

import android.content.Context
import androidx.room.Room
import com.ncw6fg.nxhw18e.pdfreader.data.db.AppDatabase
import com.ncw6fg.nxhw18e.pdfreader.data.db.FileMetadataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * create by colin
 * 2026/2/19
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pdf_reader.db"
        ).build()
    }

    @Provides
    fun provideFileMetadataDao(db: AppDatabase): FileMetadataDao {
        return db.fileMetadataDao()
    }
}