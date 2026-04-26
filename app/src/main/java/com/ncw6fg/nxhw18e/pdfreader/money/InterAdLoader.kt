package com.ncw6fg.nxhw18e.pdfreader.money

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

/**
 * create by colin 
 * 2026/3/7
 */

class InterAdLoader @AssistedInject constructor(
    @param:ApplicationContext private val context: Context,
    private val installManager: InstallManager,
    @Assisted private val adUnitId: String
) {
    private val adPool = mutableListOf<InterstitialAd>()
    private val maxCacheSize = 1
    private var isRefreshing = AtomicBoolean(false)

    // 广告加载状态的信号（用于通知等待中的协程）
    private val _adReadyChannel = MutableStateFlow(adPool.size)
    val adReadyChannel = _adReadyChannel.asStateFlow()

    private val scope = MainScope()

    /**
     * 填充广告池，直到达到最大缓存数
     */
    fun fillCache() {
        if (adPool.size >= maxCacheSize || isRefreshing.get()) return
        loadNextAd()
    }

    private fun loadNextAd() {
        if (adPool.size >= maxCacheSize) {
            isRefreshing.set(false)
            return
        }

        isRefreshing.set(true)
        Log.d("Money", "loadNextAd: current size: ${adPool.size}")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("Money", "onAdLoaded: ")
                    adPool.add(ad)
                    isRefreshing.set(false)
                    _adReadyChannel.update { adPool.size }
                    fillCache()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isRefreshing.set(false)
                    scope.launch {
                        delay(5000)
                        fillCache()
                    }
                    Log.d("Money", "onAdFailedToLoad: ${error.message}")
                }
            })
    }

    /**
     * 获取一个广告并从池子中移除
     */
    private fun popAd(): InterstitialAd? {
        val ad = if (adPool.isNotEmpty()) adPool.removeAt(0) else null
        fillCache()
        return ad?: InterAdCache.peekNativeAd(context)
    }

    private fun setupAdCallback(ad: InterstitialAd, from: String, onAdClosed: () -> Unit) {
        ad.setOnPaidEventListener {
            AnalysisUtils.logAdPaid(from, AnalysisUtils.TYPE_INTER, it, false)
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                AnalysisUtils.logAdClickedEvent(from, AnalysisUtils.TYPE_INTER)
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                onAdClosed.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onAdClosed.invoke()
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                AnalysisUtils.logAdImpressionEvent(from, AnalysisUtils.TYPE_INTER)
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.

            }
        }
    }


    suspend fun show(
        activity: Activity,
        from: String,
        timeout: Long = 5000L,
        setupLoading: (Boolean) -> Unit,
        onFinish: () -> Unit
    ) {
        if (!installManager.getRunB() &&
            AnalysisUtils.FROM_SPLASH_INTER != from
            && AnalysisUtils.FROM_CREATED_INTER != from
        ) {
            onFinish()
            return
        }
        var ad = popAd()
        if (ad == null) {
            setupLoading(true)
            fillCache()
            withTimeoutOrNull(timeout) {
                // 挂起协程，直到 adReadyChannel 发出信号
                Log.d("Money", "show: suspend，wait loading")
                adReadyChannel.first { it > 0 }
                ad = popAd()
                Log.d("Money", "show: get ad loaded: ${ad != null}")
                return@withTimeoutOrNull ad
            }
            setupLoading(false)
        }
        val showAd = ad
        Log.d("Money", "show: finally ad: ${showAd != null}")
        if (showAd != null) {
            setupAdCallback(
                showAd,
                from,
                onFinish
            )
            showAd.show(activity)
        } else {
            onFinish()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(adUnitId: String): InterAdLoader
    }
}