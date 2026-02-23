package com.ncw6fg.nxhw18e.pdfreader.data

import android.net.Uri
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentType

/**
 * create by colin
 * 2026/2/5
 */
data class DocFile(
    val id: Long,
    val name: String,
    val path: String,       // 物理路径，用于删除和外部打开
    val uri: Uri,          // 授权 Uri，用于分享和内部预览
    val size: Long,
    val mimeType: DocumentType,
    val extension: String,
    val lastModified: String,
    val isFavorite: Boolean
)
