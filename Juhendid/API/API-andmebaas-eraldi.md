# Juhend: Firebase Firestore + API Demo samas Android Studio projektis

## Eesmärk
Selle juhendi eesmärk on näidata samm-sammult, kuidas ühes ja samas Android Studio projektis hoida korraga:

- **Firebase Firestore postitused**, mis salvestuvad püsivalt
- **API Demo ekraan**, mis toob testandmeid internetist

See lahendus sobib projekti jaoks, mille package on **`com.example.blogi`**.

---

# Mida lõpptulemus teeb

Pärast selle juhendi sammude tegemist on rakenduses kaks erinevat andmevoogu:

## 1. Firestore voog
Seda kasutatakse sinu enda postituste jaoks.

- kasutaja avab `Create`
- lisab postituse
- postitus salvestatakse Firebase Firestore andmebaasi
- postitus jääb alles ka pärast rakenduse sulgemist
- `HomeScreen` loeb postitused Firestore'ist ja kuvab need

## 2. API Demo voog
Seda kasutatakse testandmete näitamiseks.

- rakendus teeb `GET` päringu test API-le
- loeb näiteks JSONPlaceholder'ist postitused
- kuvab need eraldi ekraanil
- see voog on ainult demo eesmärgil ja ei salvesta midagi Firestore'i

---

# Miks hoida neid eraldi

Kõige tähtsam arhitektuuriotsus oli see:

- **Firestore postitused** ei lähe `ApiDemoScreen` sisse
- **test API andmed** ei lähe `HomeScreen` sisse

## Õige loogika
- `HomeScreen` = Firestore postitused
- `ApiDemoScreen` = test API andmed

See teeb rakenduse loogika palju arusaadavamaks.

---

# 1. Firebase Console'is Firestore database loomine

## Ava
Firebase Console → sinu projekt → **Build → Firestore Database**

## Tee
- vajuta **Create database**
- vali **Start in test mode**
- vali piirkond
- vajuta **Enable**

## Miks see vajalik on
Ilma Firestore andmebaasita ei ole kohta, kuhu `Create` ekraanilt postitusi salvestada.

## Tulemus
Firebase loob andmebaasi, kus hakkame kasutama kollektsiooni:

```text
posts
```

---

# 2. Firestore dependency lisamine

## Fail
`app/build.gradle.kts`

## Lisa `dependencies { ... }` plokki
```kotlin
implementation("com.google.firebase:firebase-firestore")
```

## Näide
Kui sul on juba:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
implementation("com.google.firebase:firebase-auth")
```

siis lisa juurde:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
```

## Pärast seda
Tee **Sync Now**.

---

# 3. API Demo dependency'd

Kui sul ei ole veel Retrofitit lisatud, siis lisa need samasse `dependencies { ... }` plokki:

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

## Miks need vajalikud on
- `retrofit` teeb API päringute kirjutamise lihtsaks
- `converter-gson` teisendab JSON vastuse Kotlin objektideks
- `logging-interceptor` aitab näha päringuid Logcat'is

---

# 4. Õige kaustastruktuur

Varasem suur probleem oli see, et faile pandi valesse kohta.

## Vale koht
Neid faile ei tohi panna auth kaustade alla:

- `feature_auth/data/remote/...`
- `feature_auth/data/repository/...`

## Õige koht

### Firestore jaoks
- `app/src/main/java/com/example/blogi/data/model/`
- `app/src/main/java/com/example/blogi/data/repository/`
- `app/src/main/java/com/example/blogi/feature_blog/logic/`
- `app/src/main/java/com/example/blogi/feature_home/ui/`

### API Demo jaoks
- `app/src/main/java/com/example/blogi/data/remote/`
- `app/src/main/java/com/example/blogi/feature_home/logic/`
- `app/src/main/java/com/example/blogi/feature_home/ui/`

---

# 5. Firestore andmemudel

## Fail
`app/src/main/java/com/example/blogi/data/model/BlogPost.kt`

## Sisu
```kotlin
package com.example.blogi.data.model

data class BlogPost(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val authorUid: String = ""
)
```

## Miks see vajalik on
- Firestore loeb selle objekti mugavalt tagasi
- vaikeväärtused aitavad `toObject(...)` kasutamisel
- `authorUid` seob postituse sisselogitud kasutajaga

---

# 6. Firestore repository loomine

## Uus fail
`app/src/main/java/com/example/blogi/data/repository/BlogRepository.kt`

## Sisu
```kotlin
package com.example.blogi.data.repository

import com.example.blogi.data.model.BlogPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BlogRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = firestore.collection("posts")

    suspend fun addPost(title: String, content: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Kasutaja pole sisse logitud")

        val postId = System.currentTimeMillis()

        val post = BlogPost(
            id = postId,
            title = title.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            authorUid = user.uid
        )

        postsCollection
            .document(postId.toString())
            .set(post)
            .await()
    }

    suspend fun getPosts(): List<BlogPost> {
        val snapshot = postsCollection
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(BlogPost::class.java) }
            .sortedByDescending { it.createdAt }
    }
}
```

## Mida see teeb
- salvestab uue postituse Firestore'i
- loeb postitused Firestore'ist
- hoiab uuemad postitused eespool

---

# 7. Firestore ViewModel

## Fail
`app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`

## Sisu
```kotlin
package com.example.blogi.feature_blog.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogi.data.model.BlogPost
import com.example.blogi.data.repository.BlogRepository
import kotlinx.coroutines.launch

class BlogViewModel : ViewModel() {

    private val repository = BlogRepository()

    var posts by mutableStateOf<List<BlogPost>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPosts()
    }

    fun getPostById(postId: Long): BlogPost? {
        return posts.find { it.id == postId }
    }

    fun loadPosts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                posts = repository.getPosts()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Postituste laadimine ebaõnnestus"
            } finally {
                isLoading = false
            }
        }
    }

    fun addPost(
        title: String,
        content: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()

        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) {
            onError("Pealkiri ja sisu ei tohi olla tühjad")
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                repository.addPost(cleanTitle, cleanContent)
                loadPosts()
                onSuccess()
            } catch (e: Exception) {
                val message = e.message ?: "Postituse salvestamine ebaõnnestus"
                errorMessage = message
                onError(message)
            } finally {
                isLoading = false
            }
        }
    }
}
```

---

# 8. Firestore HomeScreen

## Fail
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
import com.example.blogi.data.model.BlogPost
import com.example.blogi.feature_blog.logic.BlogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun HomeScreen(
    blogViewModel: BlogViewModel,
    onPostClick: (Long) -> Unit
) {
    LaunchedEffect(Unit) {
        blogViewModel.loadPosts()
    }

    when {
        blogViewModel.isLoading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                CircularProgressIndicator()
            }
        }

        blogViewModel.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Viga: ${blogViewModel.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = { blogViewModel.loadPosts() }) {
                    Text("Proovi uuesti")
                }
            }
        }

        blogViewModel.posts.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Postitusi veel pole",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Ava Create ja lisa oma esimene postitus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(blogViewModel.posts, key = { it.id }) { post ->
                    PostCard(post = post, onPostClick = onPostClick)
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: BlogPost,
    onPostClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = { onPostClick(post.id) }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = formatDate(post.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            val preview = if (post.content.length > 220) {
                post.content.take(220) + "..."
            } else {
                post.content
            }

            Text(
                text = preview,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

## Mida see kuvab
- sinu enda Firestore postitused
- loading oleku
- error oleku
- tühja oleku

---

# 9. API Demo andmemudel

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

---

# 10. API interface

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

---

# 11. RetrofitClient

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

---

# 12. API Demo ViewModel

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

---

# 13. API Demo ekraan

## Uus fail
`app/src/main/java/com/example/blogi/feature_home/ui/ApiDemoScreen.kt`

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
fun ApiDemoScreen(
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
                        ApiPostCard(post)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiPostCard(post: TestPost) {
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

## Mida see kuvab
- internetist tulnud testandmed
- loading oleku
- error oleku
- nimekirja testpostitustest

---

# 14. Route'ide lisamine

## Fail
`app/src/main/java/com/example/blogi/core/navigation/AppDestinations.kt`

Kui see fail on sul tegelikult teises package'is, kasuta selle tegelikku asukohta. Tähtis on, et import oleks muudes failides sama package'iga kooskõlas.

## Sisu peab sisaldama
```kotlin
object AppDestinations {
    const val HOME = "home"
    const val CREATE = "create"
    const val PROFILE = "profile"
    const val API_DEMO = "api_demo"
    const val POST_DETAIL = "post_detail/{postId}"

    fun postDetailRoute(postId: Long) = "post_detail/$postId"
}
```

---

# 15. AppNavGraph ühendab mõlemad vood

## Fail
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

## Sisu
```kotlin
package com.example.blogi.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogi.feature_blog.logic.BlogViewModel
import com.example.blogi.feature_home.logic.ApiDemoViewModel
import com.example.blogi.feature_home.ui.ApiDemoScreen
import com.example.blogi.feature_home.ui.HomeScreen
import com.example.blogi.feature_postdetail.ui.PostDetailScreen
import com.example.blogi.feature_profile.ui.ProfileScreen
import com.example.blogiapp.feature_create.ui.CreateScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    blogViewModel: BlogViewModel,
    onLogoutClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            HomeScreen(
                blogViewModel = blogViewModel,
                onPostClick = { postId ->
                    navController.navigate(AppDestinations.postDetailRoute(postId))
                }
            )
        }

        composable(AppDestinations.CREATE) {
            CreateScreen(
                onSavePost = { title, content ->
                    blogViewModel.addPost(
                        title = title,
                        content = content,
                        onSuccess = {
                            navController.navigate(AppDestinations.HOME) {
                                popUpTo(AppDestinations.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onError = {
                        }
                    )
                }
            )
        }

        composable(AppDestinations.API_DEMO) {
            val apiDemoViewModel: ApiDemoViewModel = viewModel()
            ApiDemoScreen(viewModel = apiDemoViewModel)
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
                onLogoutClick = onLogoutClick,
                onApiDemoClick = {
                    navController.navigate(AppDestinations.API_DEMO)
                }
            )
        }
    }
}
```

---

# 16. MainActivity jääb puhtaks

## Fail
`app/src/main/java/com/example/blogi/MainActivity.kt`

## Oluline
`MainActivity` ei pea enam ise `ApiDemoViewModel`-it hoidma.

## Kontrolli, et seal EI oleks
```kotlin
import com.example.blogi.feature_home.logic.ApiDemoViewModel
```

ja EI oleks
```kotlin
val apiDemoViewModel: ApiDemoViewModel = viewModel()
```

## AppNavGraph kutse peab olema
```kotlin
AppNavGraph(
    navController = navController,
    darkTheme = darkTheme,
    onDarkThemeChange = onDarkThemeChange,
    blogViewModel = blogViewModel,
    onLogoutClick = onLogoutClick
)
```

---

# 17. ProfileScreen nupp API Demo avamiseks

Kui tahad API Demo ekraanile minna Profile alt, siis muuda `ProfileScreen` signatuuri.

## Fail
`app/src/main/java/com/example/blogi/feature_profile/ui/ProfileScreen.kt`

## Lisa signatuuri uus callback
```kotlin
onApiDemoClick: () -> Unit
```

## Näide
```kotlin
@Composable
fun ProfileScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onApiDemoClick: () -> Unit
) {
    ...
}
```

## Lisa ekraanile nupp
```kotlin
Button(onClick = onApiDemoClick) {
    Text("Ava API demo")
}
```

---

# 18. Väga oluline package/importi probleem

Protsessi käigus tekkis sul viga:

```text
Unresolved reference 'core'
```

See tuli sellest, et mõnes failis oli import:

```kotlin
import com.example.blogiapp.core.navigation.AppDestinations
```

aga tegelik `AppDestinations` fail oli package'is:

```kotlin
com.example.blogi.core.navigation
```

## Õige loogika
Kõik failid, mis impordivad `AppDestinations`, peavad kasutama sama package'i, kus fail päriselt asub.

### Näiteks
Kui `AppDestinations.kt` algab nii:
```kotlin
package com.example.blogi.core.navigation
```

siis import peab olema:
```kotlin
import com.example.blogi.core.navigation.AppDestinations
```

mitte:
```kotlin
import com.example.blogiapp.core.navigation.AppDestinations
```

## Kontrolli eriti neid faile
- `AppBottomBar.kt`
- `BottomBarNavigator.kt`
- `AppNavGraph.kt`
- `MainActivity.kt`

---

# 19. Kuidas mõlemad ekraanil kuvatakse

## Firestore kuvamine
- `HomeScreen` näitab Firestore postitusi
- `CreateScreen` lisab Firestore'i
- andmed jäävad alles

## API Demo kuvamine
- `ApiDemoScreen` näitab test API andmeid
- need ei salvestu Firestore'i
- need on mõeldud päringute demonstreerimiseks

---

# 20. Firestore andmete kuju

Firestore'is luuakse kollektsioon:

```text
posts
```

Dokument näiteks:

```text
posts/1710012345678
```

Väljad:
```text
id: 1710012345678
title: "Minu esimene postitus"
content: "See jääb alles ka pärast äpi sulgemist"
createdAt: 1710012345678
authorUid: "firebase_user_uid"
```

---

# 21. API Demo andmete allikas

API Demo kasutab aadressi:

```text
https://jsonplaceholder.typicode.com/posts
```

See on test-API, mis tagastab JSON kujul postitusi.

Rakendus võtab esimesed 10 kirjet:

```kotlin
posts = RetrofitClient.api.getPosts().take(10)
```

---

# 22. Kontrollnimekiri

## Firebase osa
- kas Firestore database on Firebase Console'is loodud
- kas `firebase-firestore` dependency on lisatud
- kas `BlogRepository.kt` on õiges kaustas
- kas `BlogViewModel.kt` kasutab Firestore repository't
- kas `HomeScreen.kt` näitab `BlogViewModel.posts`

## API Demo osa
- kas `TestPost.kt` on olemas
- kas `ApiService.kt` on olemas
- kas `RetrofitClient.kt` on olemas
- kas `ApiDemoViewModel.kt` on olemas
- kas `ApiDemoScreen.kt` on olemas
- kas `API_DEMO` route on lisatud

## Navigeerimine
- kas `AppNavGraph.kt` sisaldab `API_DEMO` composable'it
- kas `ProfileScreen` või muu ekraan suudab minna `API_DEMO` route'ile
- kas `MainActivity.kt` ei halda enam `ApiDemoViewModel`-it otse

## Importid
- kas `AppDestinations` import on igas failis õige package'iga
- kas projektis ei ole segamini `com.example.blogi` ja `com.example.blogiapp` valedes kohtades

---

# 23. Lõpptulemus

Kui kõik sammud on õigesti tehtud, siis:

## Home
- näitab Firestore postitusi
- Create lisab Firestore'i
- postitused jäävad alles

## API Demo
- näitab internetist tulevaid testandmeid
- aitab demonstreerida API päringuid
- on eraldi ekraanil, mitte Firestore vooga segamini

See on kõige puhtam lahendus, sest rakenduses on korraga:
- püsiv andmesalvestus Firebase'iga
- eraldi test API näide Retrofitiga
