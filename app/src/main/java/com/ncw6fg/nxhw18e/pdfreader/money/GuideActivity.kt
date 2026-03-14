package com.ncw6fg.nxhw18e.pdfreader.money

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.ui.act.MainActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.ShimmerButton
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.SimpleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@AndroidEntryPoint
class GuideActivity : ComponentActivity() {
    @Inject
    lateinit var installManager: InstallManager
    private val adFlow = MutableStateFlow<NativeAd?>(null)

    private val simpleViewModel by viewModels<SimpleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nativeLoader = NativeLoader(
            this,
            scope = lifecycleScope,
            from = AnalysisUtils.FROM_GUIDE_NATIVE
        )
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                val nativeAd = adFlow.collectAsStateWithLifecycle()
                val showAdLoading = simpleViewModel.showAdLoading.collectAsStateWithLifecycle()
                GuideScreen(
                    mutableListOf(
                        R.drawable.guide_1,
                        R.drawable.guide_2,
                        R.drawable.guide_3
                    ),
                    mutableListOf("", "", ""),
                    runB = installManager.getRunB(),
                    nativeAd.value
                ) {
                    simpleViewModel.showAD(
                        this,
                        AnalysisUtils.FROM_GUIDE_INTER
                    ) {
                        gogogo()
                    }
                }
                if (showAdLoading.value) {
                    AdDialog()
                }
            }
        }
        if (installManager.getRunB()) {
            nativeLoader.refreshAd { ad ->
                adFlow.update { ad }
            }
        }
    }


    private fun gogogo() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun GuideScreen(
    drawableList: List<Int>,
    guideTips: List<String>,
    runB: Boolean,
    nativeAd: NativeAd?,
    navToMain: () -> Unit
) {
    val ctx = LocalContext.current
    val currentIndex = remember {
        mutableIntStateOf(0)
    }
    val topMax = Modifier.fillMaxSize()
    Scaffold(modifier = topMax) {
        Column(modifier = topMax.padding(it)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = drawableList[currentIndex.intValue]),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White.copy(alpha = 0.3f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = guideTips[currentIndex.intValue],
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row {
                            drawableList.indices.forEach { id ->
                                val color =
                                    if (currentIndex.intValue == id) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .border(
                                            4.dp, color,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.size(2.dp))
                            }
                        }

                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val isLast = currentIndex.intValue == drawableList.size - 1
                    val text = if (isLast) ctx.getString(R.string.get_start) else ctx
                        .getString(R.string.next)
                    ShimmerButton(
                        Modifier
                            .width(120.dp)
                            .height(36.dp),
                        text = text
                    ) {
                        if (isLast) {
                            navToMain.invoke()
                        } else {
                            currentIndex.intValue += 1
                        }
                    }
                }
            }

            if (runB && nativeAd != null) {
                DisplaySmallNativeAdView(nativeAd)
            }

        }
    }
}