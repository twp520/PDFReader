package com.ncw6fg.nxhw18e.pdfreader.money

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ncw6fg.nxhw18e.pdfreader.PDRApplication
import com.ncw6fg.nxhw18e.pdfreader.R

/**
 * create by colin
 * 2024/8/11
 */
object InterAdCache {

    private const val CACHE_SIZE = 2
    private val cacheList = mutableListOf<InterstitialAd>()

    private val loadListener = object : InterstitialAdLoadCallback() {

        override fun onAdFailedToLoad(adError: LoadAdError) {
            if (cacheList.size < CACHE_SIZE) {
                load(PDRApplication.appContext)
            }
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            cacheList.add(interstitialAd)
            if (cacheList.size < CACHE_SIZE) {
                load(PDRApplication.appContext.applicationContext)
            }
        }
    }

    fun load(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            context.getString(R.string.cache_inter),
            adRequest,
            loadListener
        )
    }

    fun peekNativeAd(context: Context): InterstitialAd? {
        val cache = cacheList.removeFirstOrNull()
        if (cacheList.size < CACHE_SIZE) {
            load(context)
        }
        return cache
    }

}