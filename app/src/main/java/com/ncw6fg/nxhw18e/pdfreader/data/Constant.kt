package com.ncw6fg.nxhw18e.pdfreader.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * create by colin
 * 2026/2/9
 */

const val EXTRA_TYPE = "type"
const val EXTRA_IS_ALL = "isAll"
const val EXTRA_IS_BOOKMARK = "isBookmark"
const val EXTRA_TITLE = "title"

const val EXTRA_PATH = "path"

// 刷新间隔时间（毫秒），设置为5分钟
const val REFRESH_INTERVAL = 5 * 60 * 1000L

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}