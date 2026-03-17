# Juhend: API testandmete kuvamine Android Studio projektis

## Eesmärk
Selle juhendi eesmärk on näidata samm-sammult, kuidas lisada olemasolevasse Android Studio projekti lihtne API näide, mis:

- teeb internetist `GET` päringu,
- toob testandmed JSONPlaceholder API-st,
- kuvab need Home ekraanil,
- jätab olemasoleva sisselogimise, navigeerimise ja lokaalse blogiloogika alles.

See juhend on kirjutatud selle projekti järgi, mille package on **`com.example.blogi`**.

---

## Mida me tegime
Projektis oli juba olemas:

- `MainActivity`
- `AppNavGraph`
- `HomeScreen`c
- `BlogViewModel`
- auth voog
- bottom navigation
- lokaalne blogipostituste lisamine

API osa eesmärk ei olnud siduda testandmeid olemasoleva `BlogPost` mudeliga, vaid teha **eraldi API demo**, et oleks selgelt näha, kuidas päring internetist tuleb ja kuidas andmeid ekraanil näidatakse.

Sellepärast lõime eraldi:

- `TestPost` mudeli
- `ApiService`
- `RetrofitClient`
- `ApiDemoViewModel`
- uue `HomeScreen` API-demo kujul

---

# 1. Interneti loa lisamine

## Fail
`app/src/main/AndroidManifest.xml`

## Mida lisada
Lisa `<manifest>` ploki sisse järgmine rida:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Miks see vajalik on
Ilma selle loata ei tohi Android rakendusel interneti kaudu API päringuid teha.

## Tulemus
Manifesti algus näeb välja umbes selline:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        ... >
    </application>
</manifest>
```

---

# 2. Retrofit dependency’de lisamine

## Fail
`app/build.gradle.kts`

## Mida lisada
`dependencies { ... }` plokki lisa:

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

## Miks see vajalik on
- `retrofit` teeb API päringute kirjutamise lihtsaks
- `converter-gson` teisendab JSON vastuse Kotlin andmeobjektideks
- `logging-interceptor` aitab Logcat’is näha, milliseid päringuid tehakse

## Pärast lisamist
Tee Android Studios **Sync Now**.

---

# 3. Õige package ja kaustastruktuuri valimine

Selle projekti puhul oli üks suurimaid probleeme see, et uusi API faile pandi valesse kohta.

## Õige loogika
API demo failid ei tohiks minna `feature_auth` alla.

### Vale koht
- `feature_auth/data/remote/ApiService.kt`
- `feature_auth/data/remote/RetrofitClient.kt`
- `feature_auth/data/repository/BlogRepository.kt`

### Miks see vale oli
Need failid ei olnud seotud authiga, vaid üldise API testdemo loogikaga. Lisaks läksid package read ja tegelik asukoht omavahel segamini.

## Õige koht
Loo ja kasuta järgmisi kaustu:

### Uued kaustad
- `app/src/main/java/com/example/blogi/data/remote/`
- `app/src/main/java/com/example/blogi/feature_home/logic/`
- `app/src/main/java/com/example/blogi/feature_home/ui/`

---

# 4. API mudeli loomine

## Uus fail
`app/src/main/java/com/example/blogi/data/remote/TestPost.kt`

## Sisu
```kotlin
package com.example.blogi.data.remote

data class TestPost(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
```

## Miks see vajalik on
JSONPlaceholder API tagastab postitusi kujul:

- `userId`
- `id`
- `title`
- `body`

Seega peab Kotlinis olema andmeklass, mis selle struktuuriga klapib.

---

# 5. API interface loomine

## Uus fail
`app/src/main/java/com/example/blogi/data/remote/ApiService.kt`

## Sisu
```kotlin
package com.example.blogi.data.remote

import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<TestPost>
}
```

## Mida see teeb
See ütleb Retrofitile:

- tee `GET` päring endpointile `posts`
- tagasta vastus `List<TestPost>` kujul

Kui see ühendada base URL-iga, siis päring tehakse aadressile:

`https://jsonplaceholder.typicode.com/posts`

---

# 6. Retrofit kliendi loomine

## Uus fail
`app/src/main/java/com/example/blogi/data/remote/RetrofitClient.kt`

## Sisu
```kotlin
package com.example.blogi.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

## Mida see teeb
See fail:

- määrab API baasaadressi
- lisab logimise
- loob `Retrofit` objekti
- loob `ApiService` instantsi

## Väga oluline detail
Teenuse nimi on siin:

```kotlin
val api: ApiService
```

See tähendab, et hiljem peab kood kasutama:

```kotlin
RetrofitClient.api.getPosts()
```

mitte:

```kotlin
RetrofitClient.apiService.getPosts()
```

---

# 7. API demo ViewModeli loomine

## Uus fail
`app/src/main/java/com/example/blogi/feature_home/logic/ApiDemoViewModel.kt`

## Sisu
```kotlin
package com.example.blogi.feature_home.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogi.data.remote.RetrofitClient
import com.example.blogi.data.remote.TestPost
import kotlinx.coroutines.launch

class ApiDemoViewModel : ViewModel() {

    var posts by mutableStateOf<List<TestPost>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadPosts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                posts = RetrofitClient.api.getPosts().take(10)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Tekkis viga"
            } finally {
                isLoading = false
            }
        }
    }
}
```

## Mida see teeb
ViewModel:

- hoiab API-st saadud postitusi
- hoiab laadimise olekut
- hoiab võimaliku veateate
- teeb päringu funktsioonis `loadPosts()`

## Miks `take(10)`
JSONPlaceholder tagastab palju kirjeid. Näites näitame ainult esimesi 10, et Home ekraan ei läheks liiga pikaks.

---

# 8. HomeScreen ümbertegemine API demo ekraaniks

## Uus või asendatud fail
`app/src/main/java/com/example/blogi/feature_home/ui/HomeScreen.kt`

## Sisu
```kotlin
package com.example.blogi.feature_home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.data.remote.TestPost
import com.example.blogi.feature_home.logic.ApiDemoViewModel

@Composable
fun HomeScreen(
    viewModel: ApiDemoViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "API testandmed",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = { viewModel.loadPosts() },
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        ) {
            Text("Lae testandmed")
        }

        when {
            viewModel.isLoading -> {
                CircularProgressIndicator()
            }

            viewModel.errorMessage != null -> {
                Text(
                    text = "Viga: ${viewModel.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            viewModel.posts.isEmpty() -> {
                Text("Andmeid ei leitud")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.posts, key = { it.id }) { post ->
                        PostCard(post)
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: TestPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "ID: ${post.id}",
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
            )

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

## Mida see teeb
Kui Home ekraan avaneb:

- `LaunchedEffect(Unit)` kutsub `viewModel.loadPosts()`
- kuvatakse laadimise spinner
- kui andmed tulevad, kuvatakse postitused kaardina
- nupuga saab testandmed uuesti laadida

## Tähtis muutus
Vana `HomeScreen` võttis sisse:

```kotlin
posts: List<BlogPost>, onPostClick: (Long) -> Unit
```

Uus `HomeScreen` võtab sisse:

```kotlin
viewModel: ApiDemoViewModel
```

See tähendab, et ka navigeerimise fail tuli vastavalt ümber muuta.

---

# 9. AppNavGraph muutmine

## Fail
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

## Õige sisu
```kotlin
package com.example.blogi.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogi.feature_blog.logic.BlogViewModel
import com.example.blogi.feature_home.logic.ApiDemoViewModel
import com.example.blogi.feature_postdetail.ui.PostDetailScreen
import com.example.blogi.feature_profile.ui.ProfileScreen
import com.example.blogiapp.core.navigation.AppDestinations
import com.example.blogiapp.feature_create.ui.CreateScreen
import com.example.blogi.feature_home.ui.HomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    blogViewModel: BlogViewModel,
    apiDemoViewModel: ApiDemoViewModel,
    onLogoutClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            HomeScreen(viewModel = apiDemoViewModel)
        }

        composable(AppDestinations.CREATE) {
            CreateScreen(
                onSavePost = { title, content ->
                    blogViewModel.addPost(title, content)
                    navController.navigate(AppDestinations.HOME) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.POST_DETAIL) { backStackEntry ->
            val postId = backStackEntry.arguments
                ?.getString("postId")
                ?.toLongOrNull()

            val post = postId?.let { blogViewModel.getPostById(it) }

            PostDetailScreen(post = post)
        }

        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
                onLogoutClick = onLogoutClick
            )
        }
    }
}
```

## Mida see muudab
Home route ei kasuta enam `blogViewModel.posts`, vaid API demo ViewModelit.

---

# 10. MainActivity muutmine

## Fail
`app/src/main/java/com/example/blogi/MainActivity.kt`

## Olulised muudatused
Tuli juurde import:

```kotlin
import com.example.blogi.feature_home.logic.ApiDemoViewModel
```

`AppEntry()` sisse tuli juurde:

```kotlin
val apiDemoViewModel: ApiDemoViewModel = viewModel()
```

Ja `AppNavGraph(...)` kutse muutus selliseks:

```kotlin
AppNavGraph(
    navController = navController,
    darkTheme = darkTheme,
    onDarkThemeChange = onDarkThemeChange,
    blogViewModel = blogViewModel,
    apiDemoViewModel = apiDemoViewModel,
    onLogoutClick = onLogoutClick
)
```

## Miks see vajalik oli
Sest `HomeScreen` vajab nüüd `ApiDemoViewModel` objekti, mitte ainult postituste listi.

---

# 11. BlogViewModel puhastamine

## Fail
`app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`

API demo tegemise käigus tekkis viga, sest `BlogViewModel` üritas kutsuda:

```kotlin
repository.fetchPosts()
```

aga vana `BlogRepository` lahendus oli pooleli või valesti seadistatud.

## Õige lahendus
API demo hoiti täiesti eraldi `ApiDemoViewModel` sees.

Seega `BlogViewModel` jäeti ainult lokaalse blogiloogika jaoks.

## Õige sisu
```kotlin
package com.example.blogi.feature_blog.logic

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.blogi.data.model.BlogPost

class BlogViewModel : ViewModel() {

    val posts = mutableStateListOf<BlogPost>()

    fun getPostById(postId: Long): BlogPost? {
        return posts.find { it.id == postId }
    }

    fun addPost(title: String, content: String) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()

        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) return

        posts.add(
            0,
            BlogPost(
                id = System.currentTimeMillis(),
                title = cleanTitle,
                content = cleanContent,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
```

---

# 12. Miks vead tekkisid

Allpool on kokku võetud peamised vead, mis protsessi jooksul tekkisid.

## Viga 1: `Unresolved reference: RetrofitClient`

### Põhjus
`BlogRepository.kt` importis:

```kotlin
import com.example.blogi.data.remote.RetrofitClient
```

aga fail asus tegelikult vales kaustas või vale package all, näiteks `feature_auth/data/remote`.

### Lahendus
API failid tuli panna õigesse package’i:

- `com.example.blogi.data.remote`

või üldse vana `BlogRepository` lahendus eemaldada.

---

## Viga 2: `No parameter with name 'apiDemoViewModel' found`

### Põhjus
`MainActivity.kt` hakkas saatma `AppNavGraph(...)` funktsioonile uut parameetrit:

```kotlin
apiDemoViewModel = apiDemoViewModel
```

aga `AppNavGraph.kt` funktsiooni päises seda veel ei olnud.

### Lahendus
Tuli muuta `AppNavGraph` signatuuri ja lisada:

```kotlin
apiDemoViewModel: ApiDemoViewModel
```

---

## Viga 3: `Unresolved reference: fetchPosts`

### Põhjus
`BlogViewModel.kt` üritas endiselt kasutada vana repository-põhist API laadimist:

```kotlin
repository.fetchPosts()
```

aga `BlogRepository` ei sisaldanud enam seda funktsiooni või see lahendus jäi pooleli.

### Lahendus
API päringu loogika viidi täielikult `ApiDemoViewModel` sisse.
`BlogViewModel` puhastati tagasi ainult lokaalse blogi haldamiseks.

---

## Viga 4: segamini package nimed `blogi` ja `blogiapp`

### Põhjus
Projektis kasutati korraga nii:

- `com.example.blogi`
- `com.example.blogiapp`

See tekitas olukorra, kus import viitas ühele package’ile, aga fail oli teise all.

### Lahendus
Uute API failide puhul tuli hoida package järjepidevana ja kasutada samu nimesid kogu voos.

---

# 13. Lõplik kaustastruktuur

API demo jaoks vajalikud failid:

## Olemasolevad, muudetud failid
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- `app/src/main/java/com/example/blogi/MainActivity.kt`
- `app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`
- `app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`
- `app/src/main/java/com/example/blogi/feature_home/ui/HomeScreen.kt`

## Uued failid
- `app/src/main/java/com/example/blogi/data/remote/TestPost.kt`
- `app/src/main/java/com/example/blogi/data/remote/ApiService.kt`
- `app/src/main/java/com/example/blogi/data/remote/RetrofitClient.kt`
- `app/src/main/java/com/example/blogi/feature_home/logic/ApiDemoViewModel.kt`

---

# 14. Kuidas kontrollida, et kõik töötab

## Kontrollnimekiri

### Manifest
Kas `INTERNET` luba on lisatud?

### Gradle
Kas Retrofit dependency’d on lisatud ja projekt on sünkroonitud?

### Remote failid
Kas need failid on olemas?
- `TestPost.kt`
- `ApiService.kt`
- `RetrofitClient.kt`

### ViewModel
Kas `ApiDemoViewModel.kt` on olemas?

### HomeScreen
Kas `HomeScreen` võtab sisse `ApiDemoViewModel`?

### NavGraph
Kas `AppNavGraph` võtab sisse `apiDemoViewModel` parameetri?

### MainActivity
Kas `AppEntry()` loob `apiDemoViewModel` ja annab selle `AppNavGraph`-ile edasi?

---

# 15. Lõpptulemus

Kui kõik sammud on õigesti tehtud, siis rakenduses Home ekraani avamisel:

- tehakse API päring aadressile `https://jsonplaceholder.typicode.com/posts`
- kuvatakse laadimise ajal spinner
- seejärel kuvatakse esimesed 10 testpostitust
- iga postituse juures näeb:
  - ID-d
  - pealkirja
  - sisu
- nupuga saab testandmed uuesti laadida

See lahendus täidab eesmärgi näidata selgelt, kuidas Android Studio projektis API päringuid teha ja saadud andmeid Compose UI-s kuvada.
