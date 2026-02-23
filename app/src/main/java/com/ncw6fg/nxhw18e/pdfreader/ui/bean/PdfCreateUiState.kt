package com.ncw6fg.nxhw18e.pdfreader.ui.bean

import android.net.Uri

/**
 * create by colin
 * 2026/2/20
 */
data class PdfCreateUiState(
    val fileName: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val isGenerating: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 0
){
    val createButtonEnable: Boolean
        get() = fileName.isNotBlank() && selectedImages.isNotEmpty() && !isGenerating
}
