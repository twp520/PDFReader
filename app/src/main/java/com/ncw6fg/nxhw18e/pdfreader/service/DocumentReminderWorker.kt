package com.ncw6fg.nxhw18e.pdfreader.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ncw6fg.nxhw18e.pdfreader.data.util.PDFileUtil

/**
 * create by colin
 * 2026/2/22
 */

class DocumentReminderWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PDFileUtil.sendNotification(applicationContext, 2)
        return Result.success()
    }
}