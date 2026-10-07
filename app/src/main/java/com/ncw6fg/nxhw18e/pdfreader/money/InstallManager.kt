package com.ncw6fg.nxhw18e.pdfreader.money

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit


@Singleton
class InstallManager @Inject constructor(
    @param:ApplicationContext private val appContext: Context
) {

    private val storePerf = appContext.getSharedPreferences(
        "Pdr",
        Context.MODE_PRIVATE
    )
    private val TAG: String = "InstallManager"
    private var conditions = listOf(
        "apps.facebook.com",
        "apps.Instagram.com",
        "fb4a",
        "gclid",
        "not%20set",
        "not set",
        "youtubeads",
        "bytedance",
        "google/cpc",
        "direct",
        "(direct)",
    )
    private var retryCount = 0

    //utm_source=apps.facebook.com；gclid；utm_source=(not%20set)&utm_medium=(not%20set)；
    private var shouldRun: Boolean = false
    private var testRunB = false

    fun init(context: Context, initComplete: Runnable) {
        val isReview = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
        Log.d(TAG, "init: isReview = $isReview")
        // if (isReview) {
        //     shouldRun = false
        //     initComplete.run()
        //     AnalysisUtils.logEvent("app_review")
        //     return
        // }

        if (hasEnable()) {
            shouldRun = getEnable()
            Log.d(TAG, "init: ShareHelper.getEnable = $shouldRun")
            val from = if (shouldRun) "B" else "A"
            AnalysisUtils.logEvent("app_launch_local_$from")
            initComplete.run()
            return
        }
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        try {
                            val response = referrerClient.installReferrer
                            val referrerUrl = response.installReferrer
                            val enableSdk =
                                conditions.any { str -> referrerUrl.contains(str) }
                            setEnable(enableSdk)
                            Log.d(
                                TAG,
                                "onInstallReferrerSetupFinished: enableSdk=$enableSdk, referrerUrl=$referrerUrl"
                            )
                            shouldRun = enableSdk
                            initComplete.run()
                            val from = if (shouldRun) "B" else "A"
                            AnalysisUtils.logEvent("app_launch_Referrer$from")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            shouldRun = true
                            initComplete.run()
                            AnalysisUtils.logEvent("installReferrer_exception")
                        }
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                        shouldRun = false
                        initComplete.run()
                        Log.d(
                            TAG,
                            "onInstallReferrerSetupFinished: --> FEATURE_NOT_SUPPORTED"
                        )
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                        shouldRun = false
                        initComplete.run()
                        Log.d(
                            TAG,
                            "onInstallReferrerSetupFinished: --> SERVICE_UNAVAILABLE"
                        )
                    }

                    InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> {
                        shouldRun = false
                        initComplete.run()
                        Log.d(TAG, "onInstallReferrerSetupFinished: --> DEVELOPER_ERROR")
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED -> {
                        shouldRun = false
                        initComplete.run()
                        Log.d(
                            TAG,
                            "onInstallReferrerSetupFinished: --> SERVICE_DISCONNECTED"
                        )
                    }

                    else -> {
                        initComplete.run()
                    }
                }

            }

            override fun onInstallReferrerServiceDisconnected() {
                if (retryCount < 5) {
                    retryCount += 1
                    referrerClient.startConnection(this)
                }
            }

        })
    }


    fun getRunB() = testRunB || shouldRun

    fun setRunB() {
        testRunB = true
    }

    private val keyEnable = "keyEnable"

    private fun hasEnable(): Boolean {
        return storePerf.contains(keyEnable)
    }

    private fun setEnable(enable: Boolean) {
        storePerf.edit { putBoolean(keyEnable, enable) }
    }

    private fun getEnable(): Boolean {
        return storePerf.getBoolean(keyEnable, false)
    }
}