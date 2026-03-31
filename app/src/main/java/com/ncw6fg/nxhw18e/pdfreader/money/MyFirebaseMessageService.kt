package com.ncw6fg.nxhw18e.pdfreader.money

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * create by colin 
 * 2026/3/31
 */
class MyFirebaseMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebaseMessageService", "onNewToken: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {

    }
}