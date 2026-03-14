package com.ncw6fg.nxhw18e.pdfreader.money

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.ncw6fg.nxhw18e.pdfreader.R

/**
 * create by colin
 * 2024/8/5
 */
object NativeAdCache {

    private val cacheList = mutableListOf<NativeAd>()
    private const val TAG = "NativeAdCache"

    @SuppressLint("StaticFieldLeak")
    private var adLoader: AdLoader? = null

    fun init(context: Context) {
        if (adLoader != null)
            return
        val builder = AdLoader.Builder(context, context.getString(R.string.native_test))
        builder.forNativeAd { nativeAd ->
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            Log.d(TAG, "NativeAdCache refreshAd:  forNativeAd->")
            cacheList.add(nativeAd)
        }
        val videoOptions =
            VideoOptions.Builder().setStartMuted(true)
                .build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        adLoader = builder.withAdListener(
            object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "NativeAdCache onAdLoaded: ")
                    if (cacheList.size < 5) {
                        load()
                    }
                }
            }
        ).build()
        load()
    }

    fun load() {
        adLoader?.loadAd(AdRequest.Builder().build())
    }

    fun peekNativeAd(): NativeAd? {
        val cache = cacheList.removeFirstOrNull()
        if (cacheList.size < 5) {
            load()
        }
        return cache
    }

    fun destroy() {
        cacheList.forEach {
            it.destroy()
        }
        cacheList.clear()
        adLoader = null
    }
}