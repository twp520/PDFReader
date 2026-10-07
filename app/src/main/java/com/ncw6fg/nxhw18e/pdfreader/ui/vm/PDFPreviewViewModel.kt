package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.PdfPreviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * create by colin 
 * 2026/4/26
 */
@HiltViewModel
class PDFPreviewViewModel @Inject constructor() : ViewModel() {

    private val _pdfPreviewUiState = MutableStateFlow<PdfPreviewUiState?>(null)
    val pdfPreviewUiState = _pdfPreviewUiState.asStateFlow()

    fun loadFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            // 处理content scheme的URI（来自其他应用的共享）
            var fileName: String? = null
            var path: String? = null
            if (uri.scheme == "content") {
                // 获取文件名并复制到缓存目录以便后续使用
                val cursor = context.contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex =
                            it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = it.getString(nameIndex)
                        }
                    }
                }

                // 将文件复制到缓存目录以获取本地路径
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val cacheFile = File(
                        context.cacheDir,
                        "temp.pdf"
                    )
                    inputStream.use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    path = cacheFile.absolutePath
                }
            }
            _pdfPreviewUiState.update {
                PdfPreviewUiState(fileName, path)
            }
        }
    }

    fun loadFromPath(path: String?, fileName: String?) {
        _pdfPreviewUiState.value = PdfPreviewUiState(fileName, path)
    }

}