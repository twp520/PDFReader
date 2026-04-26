package com.ncw6fg.nxhw18e.pdfreader.money

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonSpace
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.ShimmerButton
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.SimpleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : ComponentActivity() {

    @Inject
    lateinit var installManager: InstallManager
    private val dataFlow = MutableStateFlow<List<Countries>>(emptyList())
    private val adFlow = MutableStateFlow<NativeAd?>(null)
    private val simpleViewModel by viewModels<SimpleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nativeLoader = NativeLoader(
            context = this,
            scope = lifecycleScope,
            from = AnalysisUtils.FROM_LANGUAGE_NATIVE,
            id = getString(R.string.language_native)
        )
        enableEdgeToEdge()
        simpleViewModel.setup(getString(R.string.language_inter))
        setContent {
            PDFReaderTheme {
                val list = dataFlow.collectAsState()
                val adState = adFlow.collectAsState()
                val showAdLoading = simpleViewModel.showAdLoading.collectAsStateWithLifecycle()
                LanguageScreen(
                    data = list.value,
                    runB = installManager.getRunB(),
                    adState.value
                ) {
                    simpleViewModel.showAD(
                        this,
                        getString(R.string.language_inter),
                        AnalysisUtils.FROM_LANGUAGE_INTER
                    ) {
                        startGuide()
                    }
                }
                if (showAdLoading.value) {
                    AdDialog()
                }
            }
        }

        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                val countries = mutableListOf<Countries>()
                val json = assets.open("countries.json").reader().readText()
                val obj = JSONObject(json)
                val array = obj.getJSONArray("countries")
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    countries.add(
                        Countries(
                            item.getString("name"),
                            item.getString("alpha-2")
                        )
                    )
                }
                countries
            }
            dataFlow.update {
                data
            }
        }

        nativeLoader.refreshAd { ad ->
            adFlow.update { ad }
        }
    }

    private fun startGuide() {
        startActivity(Intent(this, GuideActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    data: List<Countries>,
    runB: Boolean,
    nativeAd: NativeAd?,
    navToGuide: () -> Unit
) {
    val selectCountry = remember {
        mutableStateOf(data.firstOrNull())
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.choose_language),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                LazyColumn(content = {
                    items(data, key = { item -> item.code }) { item ->
                        val isSelected = selectCountry.value == item
                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                        val textColor =
                            if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable(enabled = true, onClick = {
                                    selectCountry.value = item
                                })
                                .border(
                                    1.dp, bgColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) bgColor.copy(alpha = 0.7f) else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(isSelected, onClick = {
                                selectCountry.value = item
                            })

                            Text(
                                text = item.name,
                                color = textColor
                            )
                        }
                        CommonSpace()
                    }
                    item {
                        CommonSpace(height = 60.dp)
                    }
                })
                ShimmerButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(
                            Alignment.BottomCenter
                        ),
                    stringResource(R.string.next),
                ) {
                    navToGuide.invoke()
                    AnalysisUtils.logEvent(AnalysisUtils.BUTTON_CLICK_LANGUAGE)
                }
            }
            if (runB && nativeAd != null) {
                Box(Modifier.padding(horizontal = 8.dp)) {
                    DisplaySmallNativeAdView(nativeAd)
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        AnalysisUtils.logEvent(AnalysisUtils.SCREEN_SHOW_LANGUAGE)
    }
}