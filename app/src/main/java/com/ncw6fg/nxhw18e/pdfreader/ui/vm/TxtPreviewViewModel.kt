package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/13
 */
@HiltViewModel
class TxtPreviewViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _textContent = MutableStateFlow("")
    val textContent = _textContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    fun loadTxtFile(path: String) {
        if (textContent.value.isNotEmpty())
            return
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                // 安全检查：对于超大文本文件进行简单限制，防止 OOM
                // 这里设定限制为 2MB，你可以根据需要调整
                val limitSize = 2 * 1024 * 1024L
                if (file.length() > limitSize) {
                    // 读取部分内容并提示
                    val partialContent = file.inputStream().use { input ->
                        val buffer = ByteArray(limitSize.toInt())
                        val bytesRead = input.read(buffer)
                        String(buffer, 0, bytesRead, Charsets.UTF_8)
                    }
                    _textContent.value =
                        context.getString(R.string.txt_file_large, partialContent)
                } else {
                    // 读取全部
                    _textContent.value = file.readText(Charsets.UTF_8)
                }
            } catch (e: Exception) {
                _textContent.value = context.getString(R.string.txt_read_fail, e.localizedMessage)
            } finally {
                _isLoading.value = false
            }
        }

    }
}