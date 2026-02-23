package com.ncw6fg.nxhw18e.pdfreader.ui.bean

import androidx.compose.ui.graphics.Color

/**
 * create by colin
 * 2026/2/2
 */
data class DocumentGridItemState(
    val count: Int = 0,
    val icon: Int,
    val title: Int,
    val bgColor: Color,
    val type: DocumentType
)
