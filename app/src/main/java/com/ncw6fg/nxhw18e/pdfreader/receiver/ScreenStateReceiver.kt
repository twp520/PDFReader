package com.ncw6fg.nxhw18e.pdfreader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ncw6fg.nxhw18e.pdfreader.data.REFRESH_INTERVAL
import com.ncw6fg.nxhw18e.pdfreader.data.util.PDFileUtil

/**
 * create by colin
 * 2026/2/22
 */
class ScreenStateReceiver : BroadcastReceiver() {

    private var lastRefreshTime: Long = 0

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON ||
            intent.action == Intent.ACTION_SCREEN_OFF
        ) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastRefreshTime > REFRESH_INTERVAL) {
                PDFileUtil.sendNotification(context, 1)
                lastRefreshTime = currentTime
            }
        }
    }
}