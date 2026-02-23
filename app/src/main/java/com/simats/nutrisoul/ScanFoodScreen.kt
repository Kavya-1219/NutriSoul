package com.simats.nutrisoul

import android.Manifest
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanFoodScreen(viewModel: LogFoodViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scanResult by viewModel.scanResult.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let { viewModel.analyzeFoodImage(it) }
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(context.contentResolver, it)
                } else {
                    TODO("VERSION.SDK_INT < P")
                }
                val bitmap = ImageDecoder.decodeBitmap(source)
                viewModel.analyzeFoodImage(bitmap)
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            if (cameraPermissionState.status.isGranted) {
                takePictureLauncher.launch(null)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }) {
            Text("Scan with Camera")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text("Upload from Gallery")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (scanResult.isNotEmpty()) {
            Text("Detected Foods:")
            LazyColumn {
                items(scanResult) { foodItem ->
                    Text("- ${foodItem.name}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Suggestions:")
            LazyColumn {
                items(suggestions) { suggestion ->
                    Text("- $suggestion")
                }
            }
        }
    }
}
