package com.ncw6fg.nxhw18e.pdfreader.data

/**
 * create by colin
 * 2026/2/19
 */
data class ImagePositionInfo(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val rect get() = android.graphics.RectF(left, top, right, bottom)
}
