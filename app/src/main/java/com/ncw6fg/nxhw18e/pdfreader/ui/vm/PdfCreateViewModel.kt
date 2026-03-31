package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.findActivity
import com.ncw6fg.nxhw18e.pdfreader.data.util.PDFileUtil
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.money.InterAdLoader
import com.ncw6fg.nxhw18e.pdfreader.repo.PdfCreateRepository
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.PdfCreateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/19
 */
@HiltViewModel
class PdfCreateViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    private val pdfCreateRepository: PdfCreateRepository,
    interAdFactory: InterAdLoader.Factory
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfCreateUiState())
    val uiState = _uiState.asStateFlow()

    private val interAdLoader = interAdFactory.create(appContext.getString(R.string.home_inter))

    // 更新文件名
    fun updateFileName(newName: String) {
        _uiState.update { it.copy(fileName = newName) }
    }

    // 添加图片
    fun addImages(uris: List<Uri>) {
        _uiState.update { it.copy(selectedImages = it.selectedImages + uris) }
    }

    // 删除图片
    fun removeImage(uri: Uri) {
        _uiState.update { it.copy(selectedImages = it.selectedImages.filter { item -> item != uri }) }
    }

    // 拖拽排序逻辑
    fun moveImage(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val newList = state.selectedImages.toMutableList().apply {
                if (fromIndex in indices && toIndex in indices) {
                    add(toIndex, removeAt(fromIndex))
                }
            }
            state.copy(selectedImages = newList)
        }
    }

    // 开始生成 (现在不需要传参了，直接从 state 取)
    fun startCreatePDF(context: Context) {
        val currentState = _uiState.value
        if (currentState.fileName.isBlank() || currentState.selectedImages.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    totalPages = currentState.selectedImages.size
                )
            }

            val outputPath = pdfCreateRepository.generateFilePath(context, currentState.fileName)
            val success = pdfCreateRepository.createPdfFromImages(
                context = context,
                imageItems = currentState.selectedImages,
                outputPath = outputPath,
                onPageProgress = { index, total ->
                    _uiState.update { it.copy(currentPage = index, totalPages = total) }
                }
            )

            context.findActivity()?.let { act ->
                interAdLoader.show(
                    act,
                    AnalysisUtils.FROM_CREATED_INTER,
                    setupLoading = {

                    },
                    onFinish = {
                        _uiState.update { it.copy(isGenerating = false) }
                    })
            }
            if (success) {
                PDFileUtil.shareFile(context, File(outputPath))
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.pdf_create_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}