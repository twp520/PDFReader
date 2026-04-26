package com.ncw6fg.nxhw18e.pdfreader.money

import android.content.Context
import com.ncw6fg.nxhw18e.pdfreader.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * create by colin 
 * 2026/4/3
 */
@Singleton
class Money @Inject constructor(
    @ApplicationContext context: Context,
    interAdFactory: InterAdLoader.Factory
) {

    val splashInterLoader: InterAdLoader = interAdFactory.create(context.getString(R.string.loading_inter))

    fun preload(){
        splashInterLoader.fillCache()
    }
}