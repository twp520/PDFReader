package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_IS_ALL
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_IS_BOOKMARK
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_TITLE
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_TYPE
import com.ncw6fg.nxhw18e.pdfreader.data.REFRESH_INTERVAL
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.money.InterAdLoader
import com.ncw6fg.nxhw18e.pdfreader.money.NativeLoader
import com.ncw6fg.nxhw18e.pdfreader.repo.DocRepository
import com.ncw6fg.nxhw18e.pdfreader.ui.act.FilesListActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentGridItemState
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentType
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridExcel
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridImage
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridPPT
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridPdf
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridTXT
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridWord
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/2
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val docRepository: DocRepository,
    private val interAdLoader: InterAdLoader
) : ViewModel() {
    private var _isPermissionGranted = MutableStateFlow(hasAllFilesAccess())
    val isPermissionGranted = _isPermissionGranted.asStateFlow()

    // 记录上次刷新时间
    private var lastRefreshTime: Long = 0
    val bookmarkFiles = docRepository.bookmarkFiles.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList()
    )
    val allFiles = docRepository.allDocFiles

    val isLoading = allFiles.map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    private val _showAdLoading = MutableStateFlow(false)
    val showAdLoading = _showAdLoading.asStateFlow()

    private val nativeAdLoader = NativeLoader(
        appContext,
        scope = viewModelScope,
        from = AnalysisUtils.FROM_MAIN_NATIVE,
    )

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd = _nativeAd.asStateFlow()

    fun checkPermission() {
        _isPermissionGranted.value = hasAllFilesAccess()
        if (_isPermissionGranted.value) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastRefreshTime > REFRESH_INTERVAL) {
                refreshFiles()
                lastRefreshTime = currentTime
            }
        }
    }

    val gridItems = MutableStateFlow(
        listOf(
            DocumentGridItemState(
                title = R.string.main_item_pdf,
                icon = DocumentType.PDF.icon,
                bgColor = colorMainGridPdf,
                type = DocumentType.PDF
            ),
            DocumentGridItemState(
                title = R.string.main_item_word,
                icon = DocumentType.WORD.icon,
                bgColor = colorMainGridWord,
                type = DocumentType.WORD
            ),
            DocumentGridItemState(
                title = R.string.main_item_excel,
                icon = DocumentType.EXCEL.icon,
                bgColor = colorMainGridExcel,
                type = DocumentType.EXCEL
            ),
            DocumentGridItemState(
                title = R.string.main_item_ppt,
                icon = DocumentType.PPT.icon,
                bgColor = colorMainGridPPT,
                type = DocumentType.PPT
            ),
            DocumentGridItemState(
                title = R.string.main_item_txt,
                icon = DocumentType.TXT.icon,
                bgColor = colorMainGridTXT,
                type = DocumentType.TXT
            ),
            DocumentGridItemState(
                title = R.string.main_item_image,
                icon = DocumentType.IMAGE.icon,
                bgColor = colorMainGridImage,
                type = DocumentType.IMAGE
            ),
        )
    )

    init {
        viewModelScope.launch {
            docRepository.filesByType.collect {
                val newList = mutableListOf<DocumentGridItemState>()
                val oldList = gridItems.value
                for (itemState in oldList) {
                    newList.add(itemState.copy(count = it[itemState.type]?.size ?: 0))
                }
                gridItems.update { newList }
            }
        }
        interAdLoader.fillCache()
        nativeAdLoader.refreshAd {
            _nativeAd.value = it
        }
    }

    fun refreshFiles() {
        viewModelScope.launch {
            docRepository.scanDocs()
        }
    }

    fun navigateToManageStorageSetting(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = "package:${context.packageName}".toUri()
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 预防万一，跳转到通用设置页
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent)
        }
    }

    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // Android 10 及以下不需要这个特殊权限
            true
        }
    }

    fun onGridAllClicked(
        activity: Activity,
    ) {
        viewModelScope.launch {
            interAdLoader.show(
                activity,
                from = AnalysisUtils.FROM_MAIN_BUTTON_INTER,
                setupLoading = {
                    _showAdLoading.value = it
                },
                onFinish = {
                    activity.startActivity(
                        Intent(
                            activity, FilesListActivity::class.java
                        ).apply {
                            putExtra(EXTRA_IS_ALL, true)
                            putExtra(EXTRA_TITLE, activity.getString(R.string.main_item_all))
                        })
                })
        }
    }

    fun onGridBookmarkClicked(activity: Activity) {
        viewModelScope.launch {
            interAdLoader.show(
                activity,
                from = AnalysisUtils.FROM_MAIN_BUTTON_INTER,
                setupLoading = {
                    _showAdLoading.value = it
                },
                onFinish = {
                    activity.startActivity(
                        Intent(
                            activity,
                            FilesListActivity::class.java
                        ).apply {
                            putExtra(EXTRA_IS_BOOKMARK, true)
                            putExtra(
                                EXTRA_TITLE,
                                activity.getString(R.string.main_item_bookmark)
                            )
                        }
                    )
                })
        }
    }

    fun onGridItemClicked(
        activity: Activity,
        type: DocumentType,
        title: String
    ) {

        viewModelScope.launch {
            interAdLoader.show(
                activity,
                from = AnalysisUtils.FROM_MAIN_BUTTON_INTER,
                setupLoading = {
                    _showAdLoading.value = it
                },
                onFinish = {
                    activity.startActivity(
                        Intent(
                            activity,
                            FilesListActivity::class.java
                        ).apply {
                            putExtra(EXTRA_TYPE, type)
                            putExtra(EXTRA_TITLE, title)
                        }
                    )
                })
        }
    }

    override fun onCleared() {
        super.onCleared()
        nativeAdLoader.destroy()
    }
}