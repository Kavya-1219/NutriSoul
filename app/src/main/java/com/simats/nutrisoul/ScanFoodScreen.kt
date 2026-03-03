package com.simats.nutrisoul

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanFoodScreen(viewModel: LogFoodViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                // Save bitmap to a temp file and get URI
                val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
                }
                viewModel.onImageSelected(Uri.fromFile(file))
            }
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onImageSelected(it) }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

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

        if (uiState.nutrition.isNotEmpty()) {
            Text("Detected Foods:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.nutrition) { foodItem ->
                    Text("- ${foodItem.name} (${foodItem.calories} kcal)")
                }
            }
        } else if (uiState.detectedFoods.isNotEmpty()) {
            Text("Detected Keywords:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.detectedFoods) { suggestion ->
                    Text("- $suggestion")
                }
            }
        }
    }
}
