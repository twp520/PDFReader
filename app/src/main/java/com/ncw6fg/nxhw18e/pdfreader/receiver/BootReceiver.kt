package com.ncw6fg.nxhw18e.pdfreader.receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ncw6fg.nxhw18e.pdfreader.service.ReaderService

/**
 * create by colin
 * 2026/2/22
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_USER_PRESENT
            || action == Intent.ACTION_BATTERY_CHANGED || action == Intent.ACTION_POWER_CONNECTED
            || action == Intent.ACTION_POWER_DISCONNECTED
        ) {
            val serviceIntent = Intent(context, ReaderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}