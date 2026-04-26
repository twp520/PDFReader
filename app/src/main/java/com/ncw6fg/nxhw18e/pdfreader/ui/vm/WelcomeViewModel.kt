package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.money.InterAdLoader
import com.ncw6fg.nxhw18e.pdfreader.money.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * create by colin 
 * 2026/3/11
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val money: Money
) : ViewModel() {

    private val _showAdLoading = MutableStateFlow(false)
    val showAdLoading = _showAdLoading.asStateFlow()

    init {
        Log.d("Money", "WelcomeViewModel onCreate: money=${money.hashCode()} ")
    }
    fun showAD(
        activity: Activity,
        from: String,
        timeout: Long = 5000,
        finish: () -> Unit
    ) {
        viewModelScope.launch {
            Log.d("Money", "WelcomeViewModel show: money=${money.hashCode()} ")
            money.splashInterLoader.show(
                activity,
                from,
                timeout,
                setupLoading = {
                    _showAdLoading.value = it
                },
                onFinish = finish
            )
        }
    }
}