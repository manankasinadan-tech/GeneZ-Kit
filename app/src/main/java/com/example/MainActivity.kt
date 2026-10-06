package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.MainScreen
import com.example.ui.theme.GenesisKitchenTheme
import com.example.viewmodel.GenesisViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: GenesisViewModel by viewModels()

  private val storagePermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) {
    viewModel.refreshStoragePermission()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Prompt for storage permission on startup if not already granted
    requestInitialStoragePermission()

    setContent {
      val themeMode by viewModel.themeMode.collectAsState()

      GenesisKitchenTheme(themeMode = themeMode) {
        MainScreen(viewModel = viewModel)
      }
    }
  }

  private fun requestInitialStoragePermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      storagePermissionLauncher.launch(
        arrayOf(
          android.Manifest.permission.READ_EXTERNAL_STORAGE,
          android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
      )
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshStoragePermission()
  }
}
