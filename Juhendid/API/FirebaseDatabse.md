# Juhend: Firebase Firestore andmebaasi kasutusele võtmine Android Studio projektis

## Eesmärk
Selle juhendi eesmärk on näidata samm-sammult, kuidas siduda olemasolev Android Studio projekt Firebase Cloud Firestore'iga, et `Create` ekraanil loodud postitused salvestuksid andmebaasi ja jääksid alles ka pärast rakenduse sulgemist.

See juhend on kirjutatud selle projekti järgi, mille package on **`com.example.blogi`**.

---

## Mida see lahendus teeb

Pärast selle juhendi sammude tegemist:

- `CreateScreen` salvestab uue postituse Firestore'i
- `HomeScreen` loeb postitused Firestore'ist
- postitused jäävad alles ka pärast rakenduse sulgemist
- olemasolev Firebase Auth jääb kasutusse
- iga postitus salvestab ka `authorUid` välja

---

# 1. Loo Firebase Console'is Firestore database

## Ava
Firebase Console → sinu projekt → **Build → Firestore Database**

## Tee
- vajuta **Create database**
- vali alustuseks **Start in test mode**
- vali regiooni asukoht
- vajuta **Enable**

## Miks see vajalik on
Ilma Firestore database'ita ei ole rakendusel kohta, kuhu postitusi salvestada.

## Tulemus
Firebase loob projekti sisse Firestore andmebaasi, kuhu hakkame salvestama kollektsiooni `posts`.

---

# 2. Lisa Firestore dependency

## Fail
`app/build.gradle.kts`

## Mida lisada
Lisa `dependencies { ... }` plokki järgmine rida:

```kotlin
implementation("com.google.firebase:firebase-firestore")
```

## Näide
Kui sul on juba Firebase BoM ja Auth olemas:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
implementation("com.google.firebase:firebase-auth")
```

siis tee sellest:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
```

## Miks see vajalik on
See lisab projektile Firestore Android SDK.

## Pärast lisamist
Tee Android Studios **Sync Now**.

---

# 3. Muuda `BlogPost` mudel Firestore'iga sobivaks

Firestore loeb objektid kõige paremini siis, kui andmeklassil on vaikeväärtused.

## Fail
`app/src/main/java/com/example/blogi/data/model/BlogPost.kt`

## Asenda kogu faili sisu sellega
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
- Firestore `toObject(...)` vajab mugavaks teisendamiseks vaikeväärtusi
- `authorUid` aitab siduda postituse sisselogitud kasutajaga

---

# 4. Loo õige repository kaust

Varasema töö käigus tekkis üks põhiprobleem sellest, et faile pandi valesse kohta.

## Ära tee nii
Ära pane Firestore postituste repository faili siia:

- `feature_auth/data/repository/BlogRepository.kt`

See on vale koht, sest postitused ei ole auth-funktsionaalsus.

## Tee nii
Loo järgmine kaust:

`app/src/main/java/com/example/blogi/data/repository/`

Kui see kaust on loodud, loo sinna fail `BlogRepository.kt`.

---

# 5. Loo Firestore repository

## Uus fail
`app/src/main/java/com/example/blogi/data/repository/BlogRepository.kt`

## Kleebi sisse
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
- `addPost(...)` salvestab postituse Firestore'i
- `getPosts()` loeb postitused välja
- `orderBy("createdAt")` järjestab tulemused ajatempli järgi
- lõpus pöörame need kahanevasse järjekorda, et uuemad postitused oleksid ees

---

# 6. Muuda `BlogViewModel` Firestore'i kasutama

Praegu oli sul `BlogViewModel`, mis hoidis postitusi mälus. See ei ole püsiv lahendus.

## Fail
`app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`

## Asenda kogu fail selle sisuga
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

## Mida see teeb
- laeb Firestore'ist postitused
- salvestab uue postituse Firestore'i
- hoiab `isLoading` ja `errorMessage` olekuid

---

# 7. Muuda `HomeScreen` Firestore postitusi näitama

## Fail
`app/src/main/java/com/example/blogi/feature_home/ui/HomeScreen.kt`

## Asenda kogu fail selle sisuga
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

## Mida see teeb
- laeb ekraani avamisel postitused
- näitab loading olekut
- näitab errorit
- näitab postituste listi

---

# 8. Muuda `AppNavGraph` Firestore voogu kasutama

## Fail
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

## Asenda kogu fail selle sisuga
```kotlin
package com.example.blogi.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogi.feature_blog.logic.BlogViewModel
import com.example.blogi.feature_postdetail.ui.PostDetailScreen
import com.example.blogi.feature_profile.ui.ProfileScreen
import com.example.blogi.feature_home.ui.HomeScreen
import com.example.blogiapp.core.navigation.AppDestinations
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

## Mida see teeb
- Home route kasutab Firestore postitusi
- Create route salvestab Firestore'i
- pärast edukat salvestust liigub tagasi Home ekraanile

---

# 9. Muuda `MainActivity` tagasi ainult `BlogViewModel` peale

Kui sul oli vahepeal alles API-demo `ApiDemoViewModel`, siis see tuleb Firestore voos eemaldada.

## Fail
`app/src/main/java/com/example/blogi/MainActivity.kt`

## Kontrolli, et seal EI oleks
```kotlin
import com.example.blogi.feature_home.logic.ApiDemoViewModel
```

## Kontrolli, et `AppEntry()` sees EI oleks
```kotlin
val apiDemoViewModel: ApiDemoViewModel = viewModel()
```

## Ja et `AppNavGraph(...)` kutse oleks selline
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

# 10. Mida vana API-demo failidega teha

Kui sul on alles:
- `ApiService.kt`
- `RetrofitClient.kt`
- `TestPost.kt`
- `ApiDemoViewModel.kt`

siis Firestore postituste tööle saamiseks ei pea neid kustutama.

Need ei sega, kui nad ei ole aktiivses kasutuses.

---

# 11. Kuidas andmed Firestore'is välja näevad

Pärast esimest salvestust luuakse kollektsioon:

```text
posts
```

Ja sinna dokument näiteks kujul:

```text
posts/1710012345678
```

Dokumendi väljad on näiteks:

```text
id: 1710012345678
title: "Minu esimene postitus"
content: "See postitus jääb alles."
createdAt: 1710012345678
authorUid: "firebase_user_uid"
```

---

# 12. Miks see jääb alles ka pärast rakenduse sulgemist

Postitus salvestatakse päriselt Firestore serverisse. Kui rakendus uuesti käivitub, loetakse andmed uuesti Firestore'ist.

Lisaks on Androidi Firestore kliendil offline persistence vaikimisi sees, mis aitab lokaalse vahemäluga. Kuid peamine põhjus, miks andmed alles jäävad, on see, et need on salvestatud pilveandmebaasi.

---

# 13. Veakohad, mis protsessi jooksul tekkida võivad

## Viga 1: `No parameter with name 'apiDemoViewModel' found`
### Põhjus
`MainActivity.kt` üritab ikka saata `AppNavGraph` funktsioonile vana API-demo parameetrit.

### Lahendus
Eemalda:
- `ApiDemoViewModel` import
- `val apiDemoViewModel = viewModel()`
- `apiDemoViewModel = apiDemoViewModel`

---

## Viga 2: `Unresolved reference: fetchPosts`
### Põhjus
`BlogViewModel` üritab kasutada vana repository meetodit, mida enam pole.

### Lahendus
Asenda `BlogViewModel` täielikult juhendis toodud Firestore versiooniga.

---

## Viga 3: `Unresolved reference: RetrofitClient`
### Põhjus
Vana API-demo repository oli vales package'is või vales kaustas.

### Lahendus
Ära kasuta vana `BlogRepository` faili valest kohast. Kasuta uut Firestore repository faili:
`app/src/main/java/com/example/blogi/data/repository/BlogRepository.kt`

---

## Viga 4: Firestore ei salvesta midagi
### Põhjus
Tüüpiliselt üks neist:
- Firestore database pole Firebase Console'is loodud
- kasutaja pole sisse logitud
- Firestore dependency puudub
- internet puudub

### Lahendus
Kontrolli:
- Firestore database olemasolu
- Firebase Auth login
- Gradle dependency
- Logcat errorit

---

# 14. Kontrollnimekiri

Kontrolli need kõik läbi:

- Firebase Console'is on Firestore database loodud
- `app/build.gradle.kts` sisaldab `firebase-firestore`
- `BlogPost.kt` sisaldab vaikeväärtusi
- `BlogRepository.kt` asub `data/repository`
- `BlogViewModel.kt` kasutab `BlogRepository`-t
- `HomeScreen.kt` kasutab `BlogViewModel`-it
- `AppNavGraph.kt` kasutab `blogViewModel.addPost(...)`
- `MainActivity.kt` ei kasuta enam `ApiDemoViewModel`-it

---

# 15. Lõpptulemus

Kui kõik sammud on õigesti tehtud, siis:

- kasutaja avab `Create`
- sisestab pealkirja ja sisu
- vajutab salvestamist
- postitus salvestub Firestore'i
- rakendus liigub tagasi Home ekraanile
- Home ekraan loeb postitused Firestore'ist
- postitused on alles ka pärast rakenduse sulgemist

