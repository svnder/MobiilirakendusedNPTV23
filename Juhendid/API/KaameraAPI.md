# Camera Quickstart — 3 Files Only

---

## `build.gradle.kts` (app-level)

Add these inside your `dependencies { }` block:

```kotlin
// Compose BOM
val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
implementation(composeBom)
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.8.2")

// CameraX
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// Permissions
implementation("com.google.accompanist:accompanist-permissions:0.34.0")
```

Also make sure `buildFeatures { compose = true }` is in your `android { }` block.

---

## `AndroidManifest.xml`

Add these two lines **before** the `<application>` tag:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

## `MainActivity.kt`

Replace the entire file with this minimal working camera activity:

```kotlin
package com.yourapp  // ← change to your package name

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }

            if (permissionState.status.isGranted) {
                CameraPreview(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
```

---

Sync Gradle, run on a real device, and you'll have a live camera preview.
When you're ready to add photo capture or video, refer to the full documentation.
