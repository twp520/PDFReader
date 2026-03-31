package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.money.InterAdLoader
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
class SimpleViewModel @Inject constructor(
    private val interAdFactory: InterAdLoader.Factory,
) : ViewModel() {

    private val _showAdLoading = MutableStateFlow(false)
    val showAdLoading = _showAdLoading.asStateFlow()

    private var interAdLoader: InterAdLoader? = null

    fun showAD(
        activity: Activity,
        id: String,
        from: String,
        timeout: Long = 5000,
        finish: () -> Unit
    ) {
        val loader = interAdLoader ?: interAdFactory.create(id).also {
            interAdLoader = it
        }
        viewModelScope.launch {
            loader.show(
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