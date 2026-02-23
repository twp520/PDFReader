package com.ncw6fg.nxhw18e.pdfreader.repo

import android.content.ContentUris
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.ncw6fg.nxhw18e.pdfreader.data.DocFile
import com.ncw6fg.nxhw18e.pdfreader.data.db.FileMetadata
import com.ncw6fg.nxhw18e.pdfreader.data.db.FileMetadataDao
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * create by colin
 * 2026/2/2
 */

@Singleton
class DocRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fileMetadataDao: FileMetadataDao
) {
    private val logTag = "DocRepository"

    private val dateFormat = SimpleDateFormat.getDateInstance()

    private val _allDocFiles = MutableStateFlow<List<DocFile>>(emptyList())
    val allDocFiles = _allDocFiles.asStateFlow()

    val filesByType = allDocFiles.map { it.groupBy { file -> file.mimeType } }

    val bookmarkFiles = allDocFiles.map { it.filter { file -> file.isFavorite } }

    suspend fun scanDocs() = withContext(Dispatchers.IO) {

        val fileList = mutableListOf<DocFile>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val targetTypes = DocumentType.entries.toList()

        val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        context.contentResolver.query(
            queryUri,
            projection,
            null, // 传 null 以获取所有文件，我们在循环中根据后缀精细过滤
            null,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            Log.d(logTag, "scanDocs: cursor size = ${cursor.count}")
            while (cursor.moveToNext()) {
                val path = cursor.getString(pathCol) ?: continue
                val file = File(path)
                // Log.d(logTag, "scanDocs: file = ${file.path}")
                if (file.isDirectory) continue
                // 核心逻辑：通过文件后缀过滤
                val ext = file.extension.lowercase()
                val type = targetTypes.find { it.suffix.split(",").contains(ext) }
                if (type != null && file.exists()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(queryUri, id)
                    //unit kb
                    val size = cursor.getLong(sizeCol) / 1024
                    val modifiedTime = dateFormat.format(cursor.getLong(dateCol) * 1000)
                    val isFavorite = fileMetadataDao.getMetadata(path) != null
                    fileList.add(
                        DocFile(
                            id = id,
                            name = cursor.getString(nameCol) ?: file.name,
                            path = path,
                            uri = contentUri,
                            size = size,
                            mimeType = type,
                            extension = ext,
                            lastModified = modifiedTime,
                            isFavorite = isFavorite
                        )
                    )
                }
            }
        }
        _allDocFiles.update { fileList }
    }


    suspend fun deleteFile(id: Long, path: String, uri: Uri) = withContext(Dispatchers.IO) {
        // 删除文件
        return@withContext try {
            val file = File(path)
            if (file.exists() && file.delete()) {
                // 1. 从 MediaStore 中移除索引，防止扫描出已删除的僵尸文件
                context.contentResolver.delete(
                    uri,
                    null,
                    null
                )
                // 2. 从内存列表中移除该文件，触发 UI 更新
                _allDocFiles.update { list -> list.filterNot { it.id == id } }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Delete failed: ${e.message}")
            false
        }
    }

    suspend fun favoriteFile(id: Long, path: String, isFavorite: Boolean) =
        withContext(Dispatchers.IO) {
            // 1. 更新数据库
            fileMetadataDao.updateMetadata(
                FileMetadata(
                    id = id,
                    path = path,
                    isFavorite = isFavorite
                )
            )
            // 2. 更新内存列表
            _allDocFiles.update { list ->
                list.map { file ->
                    if (file.id == id) {
                        file.copy(isFavorite = isFavorite)
                    } else file
                }
            }
        }
}