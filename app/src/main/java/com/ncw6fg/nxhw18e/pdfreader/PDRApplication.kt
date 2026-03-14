package com.ncw6fg.nxhw18e.pdfreader

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.ncw6fg.nxhw18e.pdfreader.data.PdfPageFetcher
import com.ncw6fg.nxhw18e.pdfreader.data.PdfPageKeyer
import com.ncw6fg.nxhw18e.pdfreader.data.util.PDFileUtil
import com.ncw6fg.nxhw18e.pdfreader.money.ActivityUtil
import com.ncw6fg.nxhw18e.pdfreader.money.InstallManager
import com.ncw6fg.nxhw18e.pdfreader.money.InterAdLoader
import com.ncw6fg.nxhw18e.pdfreader.money.NativeAdCache
import com.ncw6fg.nxhw18e.pdfreader.receiver.ScreenStateReceiver
import com.ncw6fg.nxhw18e.pdfreader.service.DocumentReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/2
 */
@HiltAndroidApp
class PDRApplication : Application() {

    companion object {
        lateinit var appContext: Context
    }

    @Inject
    lateinit var installManager: InstallManager

    @Inject
    lateinit var interAdLoader: InterAdLoader

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        installManager.init(this) {

        }
        ActivityUtil.initApp(this)
        Firebase.initialize(applicationContext)
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
        MobileAds.initialize(applicationContext) {
            Log.d("Money", "onCreate: MobileAds init state = $it")
        }
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("8410F190F374DF4E365D72FD74327C5F"))
                .build()
        )
        interAdLoader.fillCache()
        NativeAdCache.init(applicationContext)
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