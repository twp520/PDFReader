package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.data.DocFile
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_PATH
import com.ncw6fg.nxhw18e.pdfreader.data.util.PDFileUtil
import com.ncw6fg.nxhw18e.pdfreader.repo.DocRepository
import com.ncw6fg.nxhw18e.pdfreader.ui.act.ImagePreviewActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.act.PDFPreviewActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.act.TxtPreviewActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/7
 */
@HiltViewModel
class FileListViewModel @Inject constructor(
    private val docRepository: DocRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow<DocumentType?>(null)

    // Mode to control which source to display
    private enum class ListMode { ALL, BOOKMARK, TYPE }

    private val _mode = MutableStateFlow(ListMode.ALL)

    // Combined flow that picks the right list based on mode/selectedType
    val files = combine(
        docRepository.allDocFiles,
        docRepository.bookmarkFiles,
        docRepository.filesByType,
        _selectedType,
        _mode
    ) { allFiles, bookmarkFiles, filesByType, selectedType, mode ->
        when (mode) {
            ListMode.ALL -> allFiles
            ListMode.BOOKMARK -> bookmarkFiles
            ListMode.TYPE -> {
                if (selectedType == null) emptyList()
                else filesByType[selectedType] ?: emptyList()
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )


    // Keep a thin imperative refresh API for callers
    fun refreshFilesFromType(
        isAll: Boolean,
        isBookmark: Boolean,
        type: DocumentType?
    ) {
        viewModelScope.launch {
            when {
                isAll -> {
                    _selectedType.update { null }
                    _mode.update { ListMode.ALL }
                }

                isBookmark -> {
                    _selectedType.update { null }
                    _mode.update { ListMode.BOOKMARK }
                }

                type != null -> {
                    _selectedType.update { type }
                    _mode.update { ListMode.TYPE }
                }

                else -> {
                    // no-op: keep previous mode
                }
            }
        }
    }

    fun share(context: Context, item: DocFile) {
        val file = File(item.path)
        PDFileUtil.shareFile(context, file)
    }

    fun delete(item: DocFile) {
        viewModelScope.launch {
            val success = docRepository.deleteFile(item.id, item.path, item.uri)
            Log.d("FileListViewModel", "delete: ${item.name} = $success")
        }
    }

    fun favorite(item: DocFile) {
        viewModelScope.launch {
            val newFavorite = !item.isFavorite
            docRepository.favoriteFile(item.id, item.path, newFavorite)
        }
    }

    fun preview(context: Context, item: DocFile) {
        when (item.mimeType) {
            DocumentType.PDF -> {
                context.startActivity(Intent(context, PDFPreviewActivity::class.java).apply {
                    putExtra(EXTRA_PATH, item.path)
                })
            }

            DocumentType.IMAGE -> {
                context.startActivity(Intent(context, ImagePreviewActivity::class.java).apply {
                    putExtra(EXTRA_PATH, item.path)
                })
            }

            DocumentType.TXT -> {
                context.startActivity(Intent(context, TxtPreviewActivity::class.java).apply {
                    putExtra(EXTRA_PATH, item.path)
                })
            }

            else -> {
                PDFileUtil.openWithExternalApp(context, File(item.path))
            }
        }
    }
}