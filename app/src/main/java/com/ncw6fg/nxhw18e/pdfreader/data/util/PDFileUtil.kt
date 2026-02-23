package com.ncw6fg.nxhw18e.pdfreader.data.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.service.ReaderService
import com.ncw6fg.nxhw18e.pdfreader.ui.act.WelcomeActivity
import java.io.File

/**
 * create by colin
 * 2026/2/20
 */
object PDFileUtil {

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = getMimeType(file)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }


    fun openWithExternalApp(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Preview"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMimeType(file: File): String {
        // 1. 获取文件后缀名 (例如: "pdf", "docx")
        val extension = file.extension.lowercase()

        // 先尝试系统自带映射
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType != null) return mimeType

        // 手动补充常见的 Office 类型映射
        return when (extension) {
            "doc" -> "application/msword"
            "docx" -> "application/application/msword"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.ms-excel"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.ms-powerpoint"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    fun sendNotification(context: Context, count: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val pendingIntent = Intent(context, WelcomeActivity::class.java).let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        // 构建通知
        val notification = NotificationCompat.Builder(context, ReaderService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setLargeIcon(Icon.createWithResource(context, R.drawable.ic_launcher_round))
            .setContentTitle(context.getString(R.string.discover_new_document))
            .setContentText(context.getString(R.string.discover_new_doc_content, count))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()
        // 发送
        manager.notify(ReaderService.NOTIFICATION_ID_NOTE, notification)
    }
}