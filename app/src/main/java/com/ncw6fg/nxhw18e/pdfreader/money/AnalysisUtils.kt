package com.ncw6fg.nxhw18e.pdfreader.money

import android.os.Bundle
import androidx.core.os.bundleOf
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.ncw6fg.nxhw18e.pdfreader.PDRApplication
import java.math.BigDecimal
import java.util.Currency

/**
 * create by colin
 * 2024/8/1
 */
object AnalysisUtils {

    const val FROM_SPLASH_INTER = "splash_inter"
    const val FROM_LANGUAGE_INTER = "language_inter"
    const val FROM_GUIDE_INTER = "guide_inter"
    const val FROM_BACK_INTER = "back_inter"
    const val FROM_MAIN_BUTTON_INTER = "main_item_inter"
    const val FROM_FILE_ITEM_INTER = "file_item_inter"
    const val FROM_CREATED_INTER = "created_inter"

    const val FROM_LANGUAGE_NATIVE = "language_native"
    const val FROM_GUIDE_NATIVE = "guide_native"
    const val FROM_MAIN_NATIVE = "main_native"

    const val TYPE_NATIVE = "native"
    const val TYPE_INTER = "inter"

    private var fbSumToValue = 0.0
    private val fbLogger = AppEventsLogger.newLogger(PDRApplication.appContext)

    const val SCREEN_SHOW_SPLASH = "screen_show_splash"
    const val SCREEN_SHOW_LANGUAGE = "screen_show_language"
    const val SCREEN_SHOW_GUIDE = "screen_show_guide"
    const val SCREEN_SHOW_MAIN = "screen_show_main"
    const val SCREEN_SHOW_FILE_LIST = "screen_show_file_list"
    const val SCREEN_SHOW_PDF_PREVIEW = "screen_show_pdf_preview"

    const val BUTTON_CLICK_LANGUAGE = "button_click_language"
    const val BUTTON_CLICK_GUIDE = "button_click_guide"
    const val BUTTON_CLICK_MAIN_ITEM = "button_click_main_item"
    const val BUTTON_CLICK_FILE_ITEM = "button_click_file_item"

    fun logEvent(event: String, args: Bundle = Bundle.EMPTY) {
        val params = Bundle()
        // params.putBoolean("isRunB", InstallManager.getRunB())
        params.putAll(args)
        Firebase.analytics.logEvent(event, params)
        // Log.d("AnalysisUtils", "logEvent: $event")
    }

    fun logAdImpressionEvent(from: String, type: String) {
        val eventName = if (type == TYPE_NATIVE) "ad_native_show" else "ad_inter_show"
        val params = bundleOf()
        params.putString("placements", from)
        logEvent(eventName, params)
    }

    fun logAdClickedEvent(from: String, type: String) {
        val eventName = if (type == TYPE_NATIVE) "ad_native_click" else "ad_inter_click"
        val params = bundleOf()
        params.putString("placements", from)
        logEvent(eventName, params)
    }

    fun logAdPaid(from: String, type: String, adValue: AdValue, needImpression: Boolean) {
        val bundle = Bundle()
        val price = adValue.valueMicros.toDouble() / 1000000.0
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, price)
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        bundle.putString("placements", from)
        logEvent("Ad_Impression_Revenue", bundle)
        if (needImpression) {
            logAdImpressionEvent(from, type)
        }
        if (fbSumToValue < 0.001) {
            fbSumToValue += price
        } else {
            fbLogger.logPurchase(
                BigDecimal.valueOf(fbSumToValue),
                Currency.getInstance("USD")
            )
            fbSumToValue = 0.0
        }
    }
}