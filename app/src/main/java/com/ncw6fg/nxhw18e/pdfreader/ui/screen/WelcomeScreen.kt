package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonSpace
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.fillMax

/**
 * create by colin
 * 2026/2/21
 */

@Composable
fun WelcomeScreen() {
    Surface(modifier = fillMax) {
        Column(modifier = fillMax, horizontalAlignment = Alignment.CenterHorizontally) {

            CommonSpace(height = 200.dp)

            Image(
                modifier = Modifier.size(200.dp),
                painter = painterResource(id = R.drawable.ic_launcher_rect),
                contentDescription = "launcher"
            )

            CommonSpace(height = 80.dp)
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            CommonSpace(height = 10.dp)
            Text(
                stringResource(R.string.app_desc),
                style = MaterialTheme.typography.bodyMedium
            )
            CommonSpace(height = 60.dp)
            LinearProgressIndicator(
                modifier = Modifier.size(300.dp, 8.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

    }
    LaunchedEffect(Unit) {
        AnalysisUtils.logEvent(AnalysisUtils.SCREEN_SHOW_SPLASH)
    }
}