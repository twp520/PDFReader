package com.ncw6fg.nxhw18e.pdfreader.money

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.ncw6fg.nxhw18e.pdfreader.ui.act.MainActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.act.WelcomeActivity

object ActivityUtil {
    var activityCount = 0
    private var mainLaunch = false

    fun initApp(application: Application) {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is MainActivity) {
                    mainLaunch = true
                }
            }

            override fun onActivityStarted(activity: Activity) {
                activityCount++

                if (activityCount == 1) {
                    if (mainLaunch) {
                        //热启动
                        if (activity !is WelcomeActivity) {
                            hotLoading(activity)
                        }
                    } else {
                        //冷启动
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity is MainActivity) {
                    mainLaunch = false
                }
            }

        })
    }

    fun startToMain(activity: Activity) {
        if (!mainLaunch) {
            val showLanguage = Firebase.remoteConfig.getBoolean("showLanguage")
            val showGuide = Firebase.remoteConfig.getBoolean("showGuide")
            val cls =
                when {
                    showLanguage -> LanguageActivity::class.java
                    showGuide -> GuideActivity::class.java
                    else -> MainActivity::class.java
                }
            activity.startActivity(Intent(activity, cls).apply {
                putExtra("cold_start", true)
            })
        }
        activity.finish()
    }

    private fun hotLoading(activity: Activity) {
        activity.startActivity(Intent(activity, WelcomeActivity::class.java))
    }
}