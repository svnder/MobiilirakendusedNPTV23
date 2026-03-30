# Android CameraX ja Jetpack Compose: Täielik Õpetus

See dokumentatsioon kirjeldab, kuidas ehitada kaasaegne Androidi kaamerarakendus, kasutades **CameraX** teeki ning **Jetpack Compose** kasutajaliidest. Rakendus võimaldab pildistada, vahetada eesmise ja tagumise kaamera vahel ning vaadata tehtud pilte lokaalsest galeriist.

## 1. Sõltuvuste seadistamine (`build.gradle.kts` - Module :app)

Lisa `dependencies` plokki CameraX ja Coil (piltide laadimiseks). Pärast lisamist vajuta Android Studios **Sync Now**:

~~~kotlin
val camerax_version = "1.4.0-alpha04" // Või hetke uusim stabiilne versioon

implementation("androidx.camera:camera-core:${camerax_version}")
implementation("androidx.camera:camera-camera2:${camerax_version}")
implementation("androidx.camera:camera-lifecycle:${camerax_version}")
implementation("androidx.camera:camera-view:${camerax_version}")

// Coil piltide kuvamiseks galeriis
implementation("io.coil-kt:coil-compose:2.6.0")
~~~

## 2. Load (`AndroidManifest.xml`)

Lisa enne `<application>` plokki vajalikud riistvara õigused ja kaamera luba:

~~~xml
<uses-feature android:name="android.hardware.camera.any" />
<uses-permission android:name="android.permission.CAMERA" />
~~~

## 3. Täielik rakenduse kood (`MainActivity.kt`)

Kopeeri kogu allolev kood oma `MainActivity.kt` faili. Kood sisaldab navigeerimist avalehe, kaamera ja galerii vahel, kaamera elutsükli haldust ning piltide salvestamist seadme vahemällu. Veendu, et faili esimene rida (`package ...`) vastaks sinu projekti nimele.

~~~kotlin
package com.example.cameraapi // Muuda vastavalt oma projekti paketinimele!

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.io.File

// Defineerime navigeerimise olekud
enum class AppScreen {
    HOME, CAMERA, GALLERY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                onOpenAppCamera = { currentScreen = AppScreen.CAMERA },
                onOpenAppGallery = { currentScreen = AppScreen.GALLERY }
            )
        }
        AppScreen.CAMERA -> {
            BackHandler { currentScreen = AppScreen.HOME }
            CameraScreenWrapper(onBackClick = { currentScreen = AppScreen.HOME })
        }
        AppScreen.GALLERY -> {
            BackHandler { currentScreen = AppScreen.HOME }
            GalleryScreen(onBackClick = { currentScreen = AppScreen.HOME })
        }
    }
}

@Composable
fun HomeScreen(onOpenAppCamera: () -> Unit, onOpenAppGallery: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Minu Kaamerarakendus",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ElevatedButton(
            onClick = onOpenAppCamera,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(bottom = 16.dp)
                .height(56.dp)
        ) {
            Text("Ava kaamera", style = MaterialTheme.typography.titleMedium)
        }

        FilledTonalButton(
            onClick = onOpenAppGallery,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
        ) {
            Text("Vaata galeriid", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CameraScreenWrapper(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraContent(onBackClick = onBackClick)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Kaamera luba on vajalik.", modifier = Modifier.padding(16.dp))
            Button(onClick = onBackClick) {
                Text("Tagasi avalehele")
            }
        }
    }
}

@Composable
fun CameraContent(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    // Olek eesmise/tagumise kaamera jaoks
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Kaamera sidumine ebaõnnestus", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(vertical = 24.dp, horizontal = 32.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Tagasi",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            FloatingActionButton(
                onClick = { takePhoto(context, imageCapture) },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
            ) {}

            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Pööra kaamerat",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var photoFiles by remember { mutableStateOf(emptyList<File>()) }

    // Loeme failid alati uuesti, kui galerii avatakse
    LaunchedEffect(Unit) {
        photoFiles = context.cacheDir.listFiles { file -> file.extension == "jpg" }
            ?.sortedByDescending { it.lastModified() }
            ?.toList() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minu galerii") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tagasi")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (photoFiles.isEmpty()) {
                Text(
                    text = "Pilte pole veel tehtud.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(photoFiles) { file ->
                        AsyncImage(
                            model = file,
                            contentDescription = "Salvestatud foto",
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture) {
    val photoFile = File(
        context.cacheDir,
        "Foto_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Klõps! Pilt salvestatud.", Toast.LENGTH_SHORT).show()
            }
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Viga pildistamisel", Toast.LENGTH_SHORT).show()
                Log.e("CameraX", "Pildistamise viga", exc)
            }
        }
    )
}
~~~
