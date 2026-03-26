# CameraX + Jetpack Compose juhend

## 1. Fail: app/build.gradle.kts

Lisa sõltuvused:

    dependencies {
        val cameraxVersion = "1.3.1"

        implementation("androidx.camera:camera-core:$cameraxVersion")
        implementation("androidx.camera:camera-camera2:$cameraxVersion")
        implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
        implementation("androidx.camera:camera-view:$cameraxVersion")
    }

---

## 2. Fail: app/src/main/AndroidManifest.xml

Lisa õigused ja feature:

    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools">

        <uses-permission android:name="android.permission.CAMERA" />

        <uses-feature
            android:name="android.hardware.camera"
            android:required="false" />

        <application>
        </application>
    </manifest>

---

## 3. Fail: app/src/main/java/com/example/kaamerarakendus/MainActivity.kt

Asenda faili sisu:

    package com.example.kaamerarakendus

    import android.Manifest
    import android.content.pm.PackageManager
    import android.os.Bundle
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.camera.core.*
    import androidx.camera.lifecycle.ProcessCameraProvider
    import androidx.camera.view.PreviewView
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.*
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.viewinterop.AndroidView
    import androidx.core.content.ContextCompat
    import java.io.File
    import java.text.SimpleDateFormat
    import java.util.*
    import java.util.concurrent.ExecutorService
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

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) showCamera = true
            else Toast.makeText(context, "Kaamera õigus on vajalik", Toast.LENGTH_SHORT).show()
        }

        if (!showCamera) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) showCamera = true
                    else launcher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Ava kaamera")
                }
            }
        } else {
            CameraPreview()
        }
    }

    @Composable
    fun CameraPreview() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val imageCapture = remember { ImageCapture.Builder().build() }
        val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

        Box(Modifier.fillMaxSize()) {

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = {
                    val name = SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ss",
                        Locale.US
                    ).format(System.currentTimeMillis())

                    val mediaDir = context.externalMediaDirs.firstOrNull()
                        ?: context.filesDir

                    val file = File(mediaDir, "$name.jpg")

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                    imageCapture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {

                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                Toast.makeText(
                                    context,
                                    "Salvestatud: ${file.absolutePath}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
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

---

## 4. Failistruktuur

    app/
     ├── build.gradle.kts
     ├── src/
     │    └── main/
     │         ├── AndroidManifest.xml
     │         └── java/com/example/kaamerarakendus/MainActivity.kt
