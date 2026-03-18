# Android Studio projekt algusest lõpuni: API päring ja ilusad kaardid

See juhend näitab kõik sammud algusest lõpuni ja rõhutab täpselt:

- **mis faili** tuleb avada
- **kus kaustas** see fail asub
- **mida** sinna lisada või asendada
- kuidas lõpuks teha töötav äpp, mis:
  - teeb API päringu
  - loeb JSON-andmed
  - kuvab andmed ilusate kaartidena ekraanil

See sobib Android Studio projektile, kus kasutad **Empty Activity** + **Jetpack Compose'i**.

---

## 1. Loo uus projekt

Ava **Android Studio** ja tee nii:

1. Vajuta **New Project**
2. Vali **Empty Activity**
3. Vajuta **Next**
4. Täida väljad, näiteks:
   - **Name:** `ApiCardsApp`
   - **Package name:** `com.example.apicardsapp`
   - **Language:** `Kotlin`
   - **Minimum SDK:** vali sobiv
5. Vajuta **Finish**

Nüüd loob Android Studio sulle tühja projekti.

---

## 2. Oota kuni projekt laeb ära

Enne muutmist oota, kuni:
- Gradle sync lõpeb
- kõik failid on vasakul nähtavad
- Android Studio ei lae enam taustal

---

## 3. Vaheta Project vaade õigeks

Vasakul failipuus pane vaade näiteks:

- **Android**
või
- **Project**

Alguses on lihtsam kasutada **Android** vaadet, sest siis näed peamised failid lihtsamini üles.

---

## 4. Failid, mida sa muudad

Selles juhendis muudad peamiselt neid faile:

### Fail 1
**Faili nimi:**
```text
AndroidManifest.xml
```

**Asukoht Android vaates:**
```text
app > manifests > AndroidManifest.xml
```

**Asukoht Project vaates:**
```text
app/src/main/AndroidManifest.xml
```

---

### Fail 2
**Faili nimi:**
```text
build.gradle.kts
```

**Asukoht Android vaates:**
```text
Gradle Scripts > build.gradle.kts (Module :app)
```

**Asukoht Project vaates:**
```text
app/build.gradle.kts
```

> Tähtis: muuda just **Module :app** faili, mitte projekti ülemist Gradle faili.

---

### Fail 3
**Faili nimi:**
```text
MainActivity.kt
```

**Asukoht Android vaates:**
```text
app > kotlin+java > sinu_package_nimi > MainActivity.kt
```

Näiteks:
```text
app > kotlin+java > com.example.apicardsapp > MainActivity.kt
```

**Asukoht Project vaates:**
```text
app/src/main/java/com/example/apicardsapp/MainActivity.kt
```

Mõnes projektis võib see olla ka:
```text
app/src/main/kotlin/com/example/apicardsapp/MainActivity.kt
```

---

## 5. Lisa interneti luba

### Ava see fail:
**Android vaates:**
```text
app > manifests > AndroidManifest.xml
```

**Project vaates:**
```text
app/src/main/AndroidManifest.xml
```

### Mida teha:
Lisa see rida `<manifest>` ploki sisse, enne `<application>` blokki:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Näide täielikumast failist:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.ApiCardsApp">
        ...
    </application>

</manifest>
```

### Mida see tähendab:
See annab äpile loa internetti kasutada. Ilma selleta API päringud ei tööta.

---

## 6. Lisa OkHttp dependency

### Ava see fail:
**Android vaates:**
```text
Gradle Scripts > build.gradle.kts (Module :app)
```

**Project vaates:**
```text
app/build.gradle.kts
```

### Leia sealt:
```kotlin
dependencies {
    ...
}
```

### Lisa sinna sisse see rida:
```kotlin
implementation("com.squareup.okhttp3:okhttp:5.0.0")
```

### Näide:
```kotlin
dependencies {
    implementation("androidx.core:core-ktx:...")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:...")
    implementation("androidx.activity:activity-compose:...")
    implementation("androidx.compose.ui:ui:...")
    implementation("androidx.compose.material3:material3:...")

    implementation("com.squareup.okhttp3:okhttp:5.0.0")
}
```

### Pärast seda:
Vajuta üleval ribal **Sync Now**.

### Mida see tähendab:
See lisab projekti võrguühenduse teegi, millega saad API päringuid teha.

---

## 7. Asenda MainActivity.kt täielikult

### Ava see fail:
**Android vaates:**
```text
app > kotlin+java > sinu_package_nimi > MainActivity.kt
```

Näiteks:
```text
app > kotlin+java > com.example.apicardsapp > MainActivity.kt
```

**Project vaates:**
```text
app/src/main/java/com/example/apicardsapp/MainActivity.kt
```

või mõnes projektis:
```text
app/src/main/kotlin/com/example/apicardsapp/MainActivity.kt
```

### Mida teha:
Kustuta vana sisu täielikult ja kleebi asemele järgmine kood:

```kotlin
package com.example.apicardsapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

data class Post(
    val title: String,
    val body: String
)

class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ApiScreen()
            }
        }
    }

    @Composable
    fun ApiScreen() {
        var loading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var posts by remember { mutableStateOf(listOf<Post>()) }

        LaunchedEffect(Unit) {
            fetchPosts(
                onSuccess = { result ->
                    posts = result
                    loading = false
                },
                onError = { error ->
                    errorMessage = error
                    loading = false
                }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "API postitused",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    loading -> {
                        CircularProgressIndicator()
                    }

                    errorMessage != null -> {
                        Text(
                            text = "Viga: $errorMessage",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(posts) { index, post ->
                                PostCard(
                                    number = index + 1,
                                    post = post
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PostCard(number: Int, post: Post) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Postitus #$number",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    private fun fetchPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                mainHandler.post {
                    onError(e.message ?: "Tundmatu viga")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        mainHandler.post {
                            onError("HTTP ${response.code}")
                        }
                        return
                    }

                    val responseBody = response.body.string()
                    val jsonArray = JSONArray(responseBody)
                    val result = mutableListOf<Post>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        result.add(
                            Post(
                                title = item.getString("title"),
                                body = item.getString("body")
                            )
                        )
                    }

                    mainHandler.post {
                        onSuccess(result)
                    }
                }
            }
        })
    }
}
```

---

## 8. Väga oluline: kontrolli package nime

Faili kõige esimene rida on:

```kotlin
package com.example.apicardsapp
```

See peab olema **täpselt sama**, mis sinu projekti package nimi.

### Kust seda näed:
Android vaates:
```text
app > kotlin+java > sinu_package_nimi
```

Kui sinu package on näiteks:
```text
com.example.myapplication
```

siis faili esimene rida peab olema:

```kotlin
package com.example.myapplication
```

Muidu tuleb viga.

---

## 9. Kas pead veel muid faile muutma?

Selle näite jaoks **ei pea**.

Sa ei pea praegu muutma:
- `themes`
- `colors`
- `strings.xml`
- `libs.versions.toml`

Piisab neist kolmest failist:
- `AndroidManifest.xml`
- `build.gradle.kts (Module :app)`
- `MainActivity.kt`

---

## 10. Salvesta failid

Pärast muudatusi salvesta failid:

- `Ctrl + S` Windowsis
- `Cmd + S` Macis

Salvesta vähemalt:
- `AndroidManifest.xml`
- `build.gradle.kts`
- `MainActivity.kt`

---

## 11. Tee Gradle Sync

Kui lisasid `build.gradle.kts` faili dependency, siis tee:

- vajuta **Sync Now**
või
- menüüst **File > Sync Project with Gradle Files**

See samm on vajalik, et OkHttp päriselt projektis kasutusele tuleks.

---

## 12. Käivita äpp

### Kus nupp asub:
Üleval Android Studio ribal on roheline **Run** nupp.

### Mida teha:
1. Vali emulator või telefon
2. Vajuta **Run**
3. Oota kuni äpp buildib ja avaneb

---

## 13. Mida sa ekraanil näed

Kui kõik on õigesti tehtud, siis:

1. algul näed laadimisikooni
2. siis tehakse API päring aadressile:

```text
https://jsonplaceholder.typicode.com/posts
```

3. pärast seda kuvatakse ekraanile kaardid

Iga kaart sisaldab:
- postituse numbrit
- pealkirja
- sisu

---

## 14. Kiire failide kokkuvõte

### 1) Interneti luba
**Fail:**
```text
app/src/main/AndroidManifest.xml
```

**Lisa sinna:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

### 2) OkHttp dependency
**Fail:**
```text
app/build.gradle.kts
```

**Lisa `dependencies` plokki:**
```kotlin
implementation("com.squareup.okhttp3:okhttp:5.0.0")
```

---

### 3) Kogu põhiline äpi loogika
**Fail:**
```text
app/src/main/java/sinu/package/nimi/MainActivity.kt
```

**Asenda kogu fail selle koodiga, mis juhendis üleval on.**

---

## 15. Kui midagi ei tööta

Kontrolli neid asju.

### A. Muutsid vale Gradle faili
Õige fail on:
```text
build.gradle.kts (Module :app)
```

Mitte projekti peamine Gradle fail.

---

### B. Package nimi ei klapi
`MainActivity.kt` esimene rida peab vastama sinu päris package nimele.

---

### C. INTERNET permission puudub
Ilma selleta API päring ei tööta.

---

### D. Sync jäi tegemata
Pärast dependency lisamist peab tegema **Sync Now**.

---

### E. Muutsid vale MainActivity faili
Mõnikord on projektis mitu package kausta. Muuda seda faili, mis päriselt sinu äpi package all asub.

---

## 16. Kuidas aru saada, et oled õiges kohas

Kui sa avad õige `MainActivity.kt`, siis näed seal tavaliselt midagi sarnast:

```kotlin
class MainActivity : ComponentActivity()
```

Kui avad õige `build.gradle.kts (Module :app)`, siis näed seal:
```kotlin
android {
    ...
}

dependencies {
    ...
}
```

Kui avad õige `AndroidManifest.xml`, siis näed seal:
```xml
<manifest ...>
    <application ...>
```

---

## 17. Mida see projekt teeb

See projekt:
- käivitub tühjast Empty Activity projektist
- kasutab internetti
- teeb API päringu
- loeb JSON andmeid
- muudab need `Post` objektideks
- kuvab need Compose'i kaartidena

---

## 18. Mis on järgmine samm

Kui see töötab, siis saad edasi lisada:
- nupp **Lae uuesti**
- otsing
- detailvaade
- Retrofit
- Firebase
- oma API

---

## 19. Väga lühike kontrollnimekiri

Enne Run vajutamist kontrolli:

- [ ] Muutsin faili `app/src/main/AndroidManifest.xml`
- [ ] Lisatud on `INTERNET` permission
- [ ] Muutsin faili `app/build.gradle.kts`
- [ ] Lisatud on `implementation("com.squareup.okhttp3:okhttp:5.0.0")`
- [ ] Muutsin õiget `MainActivity.kt` faili
- [ ] `package` nimi klapib
- [ ] Vajutasin `Sync Now`
- [ ] Käivitasin äpi

---

## 20. Kokkuvõte

Selles juhendis said täpselt teada:

- **mis faili** avada
- **kus see fail asub**
- **mida sinna lisada**
- **millal salvestada**
- **millal teha Sync**
- **millal Run vajutada**

Selle järgi saad nullist üles ehitada töötava Android Studio äpi, mis kuvab API andmed ilusate kaartidena.
