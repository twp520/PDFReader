package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.RootScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("MainActivity", "notification: $isGranted")
    }

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                RootScreen(mainViewModel)
            }
        }
        checkAndRequestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.checkPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        // 通知权限仅在 Android 13 (Tiramisu, API 33) 及以上版本需要申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // 情况 A: 已经获取权限
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已存在，可以执行发通知的操作
                }

                // 情况 B: 用户之前拒绝过，可能需要向用户解释为什么需要权限
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // 这里可以弹出一个对话框说明原因，然后再启动申请
                    showPermissionRationaleDialog()
                }

                // 情况 C: 直接申请权限
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.notification_alert_title))
            .setMessage(getString(R.string.notification_alert_content))
            .setPositiveButton(getString(R.string.string_confirm)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton(getString(R.string.string_cancel), null)
            .show()
    }
}