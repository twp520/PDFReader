package com.ncw6fg.nxhw18e.pdfreader

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.ncw6fg.nxhw18e.pdfreader.data.PdfPageFetcher
import com.ncw6fg.nxhw18e.pdfreader.data.PdfPageKeyer
import com.ncw6fg.nxhw18e.pdfreader.data.util.PDFileUtil
import com.ncw6fg.nxhw18e.pdfreader.receiver.ScreenStateReceiver
import com.ncw6fg.nxhw18e.pdfreader.service.DocumentReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

/**
 * create by colin
 * 2026/2/2
 */
@HiltAndroidApp
class PDRApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this)
                .components {
                    add(PdfPageFetcher.Factory())
                    add(PdfPageKeyer())
                }
                .build()
        }
        PDFileUtil.createNotificationChannel(this)
        scheduleDocumentReminder(this)
        registerReceiver(ScreenStateReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            // addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    fun scheduleDocumentReminder(context: Context) {
        val reminderRequest = PeriodicWorkRequestBuilder<DocumentReminderWorker>(
            1, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true) // 仅在电量不低时执行
                .build()
        ).build()

        // 提交任务
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DocumentReminderWork",
            ExistingPeriodicWorkPolicy.KEEP, // 如果任务已存在则保留，不重复创建
            reminderRequest
        )
    }
}