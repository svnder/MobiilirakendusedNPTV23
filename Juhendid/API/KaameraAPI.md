# CameraX integratsioon Jetpack Compose'iga

Käesolev juhend kirjeldab CameraX API ja Jetpack Compose'i ühist rakendamist. Lahendus sisaldab olekupõhist (state-driven) kasutajaliidese loogikat. Rakenduse käivitamisel kuvatakse nupp "Ava kaamera". Nupuvajutusel kontrollitakse õigusi, initsialiseeritakse kaamera ja asendatakse vaade reaalajas eelvaate ning pildistamisnupuga.

## 1. Sõltuvuste deklareerimine

**Faili asukoht:** `app/build.gradle.kts` (Module: app)

Veendu, et Compose'i baassõltuvused on projektis olemas, ning lisa `dependencies` plokki CameraX teegid. Pärast redigeerimist teosta Gradle sünkroniseerimine.

```kotlin
dependencies {
    // ... Compose'i ja muud olemasolevad sõltuvused ...
    
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")
}
```

## 2. Õiguste ja riistvara nõuete konfigureerimine

**Faili asukoht:** `app/src/main/AndroidManifest.xml`

Manifestifaili tuleb lisada kaamera kasutamise luba ning määrata riistvara kättesaadavuse nõue. Kirjed paigutada enne `<application>` märgistust.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="[http://schemas.android.com/apk/res/android](http://schemas.android.com/apk/res/android)"
    xmlns:tools="[http://schemas.android.com/tools](http://schemas.android.com/tools)">

    <uses-feature
        android:name="android.hardware.camera"
        android:required="true" />
    <uses-permission android:name="android.permission.CAMERA" />

    <application>
        </application>
</manifest>
```

## 3. Programmi äriloogika ja kasutajaliides

**Faili asukoht:** `app/src/main/java/<sinu_paketi_nimi>/MainActivity.kt`

Kuna Jetpack Compose'i puhul XML-faile ei kasutata, defineeritakse kogu kasutajaliides ja loogika Kotlini koodis. Asenda faili sisu alljärgnevaga. Muuda esimesel real asuv `package` direktiiv vastavaks loodud projektile.

```kotlin
package com.example.kaamerarakendus // ASENDA OMA PAKETI NIMEGA

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraAppScreen()
        }
    }
}

@Composable
fun CameraAppScreen() {
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCamera = true
        } else {
            Toast.makeText(context, "Kaamera õigus on nõutud.", Toast.LENGTH_SHORT).show()
        }
    }

    if (!showCamera) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Button(onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    showCamera = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text("Ava kaamera")
            }
        }
    } else {
        CameraPreviewScreen()
    }
}

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("KaameraCompose", "Kaamera sidumine ebaõnnestus", exc)
                }

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = {
                val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                    .format(System.currentTimeMillis())
                val photoFile = File(context.externalMediaDirs.firstOrNull(), "$name.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("KaameraCompose", "Faili kirjutamine ebaõnnestus", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Toast.makeText(
                                context,
                                "Salvestatud: ${photoFile.absolutePath}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        ) {
            Text("Tee pilt")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }
}
```
