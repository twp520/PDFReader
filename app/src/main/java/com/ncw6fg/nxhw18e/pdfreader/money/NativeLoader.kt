package com.ncw6fg.nxhw18e.pdfreader.money

import android.content.Context
import android.util.Log
import androidx.core.util.Function
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.ncw6fg.nxhw18e.pdfreader.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * create by colin
 * 2024/7/21
 */
class NativeLoader(
    private val context: Context,
    private val id: String,
    private val scope: CoroutineScope,
    private val from: String,
    private val needRefresh: Boolean = true,
    private val needDestroyPrevious: Boolean = true
) {

    private val maxRetryCount = 5
    private var retryCount = 1
    private val TAG = "NativeLoader"
    private var currentNativeAd: NativeAd? = null

    fun refreshAd(function: Function<NativeAd, Unit>?) {
        retryCount = 1
        val current = currentNativeAd ?: NativeAdCache.peekNativeAd()
        if (current != null) {
            function?.apply(current)
        }
        val builder = AdLoader.Builder(context, id)
        builder.forNativeAd { nativeAd ->
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            Log.d(TAG, "refreshAd:  forNativeAd->")
            nativeAd.setOnPaidEventListener { value ->
                AnalysisUtils.logAdPaid(from, AnalysisUtils.TYPE_NATIVE, value, false)
            }
            if (needDestroyPrevious) {
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd
            }
            function?.apply(nativeAd)
        }

        val videoOptions =
            VideoOptions.Builder().setStartMuted(true)
                .build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(
            object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "onAdLoaded: ")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(
                        TAG,
                        "Native onAdFailedToLoad: ${loadAdError.code} : ${loadAdError.message}"
                    )

                    if (retryCount < maxRetryCount) {
                        //retry
                        delayLoad(function = function)
                    }

                }

                override fun onAdImpression() {
                    Log.d(TAG, "Native onAdImpression: ")
                    AnalysisUtils.logAdImpressionEvent(from, AnalysisUtils.TYPE_NATIVE)
                    if (needRefresh) {
                        delayLoad(
                            delay = 15000,
                            force = true,
                            function = function
                        )
                    }
                }

                override fun onAdClicked() {
                    Log.d(TAG, "onAdClicked: $from")
                    AnalysisUtils.logAdClickedEvent(from, AnalysisUtils.TYPE_NATIVE)
                }
            }
        ).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun delayLoad(
        delay: Long = 10000,
        force: Boolean = false,
        function: Function<NativeAd, Unit>?
    ) {
        if (retryCount > maxRetryCount)
            retryCount = 1
        scope.launch {
            val time = if (force) {
                delay
            } else {
                delay * retryCount
            }
            delay(time)
            retryCount++
            refreshAd(function)
        }
    }

    fun destroy() {
        currentNativeAd?.destroy()
        currentNativeAd = null
    }
}