# BlogiApp — Samm 6: Home Click Event ja Postituse Detailvaade
## Ametlik juhend

## Eesmärk

Rakendada täielik voog, kus kasutaja vajutab Home ekraanil postituse kaardile ja avatakse sama postituse detailvaade.

Dokument kirjeldab kogu lahenduse tervikuna:
- route definitsioon
- klikisündmus Home ekraanil
- navigeerimine route argumendiga
- detailvaate ekraan
- postituse leidmine ViewModelist

---

## Arhitektuurne põhimõte

Home ekraan ei navigeeri otse.  
Home ekraan saadab ainult sündmuse (`onPostClick(postId)`), ja navigeerimise otsus tehakse `AppNavGraph` tasemel.  
See hoiab UI kihid lihtsad ning naviloogika tsentraliseeritud.

---

## 1) Route definitsioon detailvaatele

**Fail:**  
`app/src/main/java/com/example/blogi/core/navigation/AppDestinations.kt`

**Kood:**
```kotlin
package com.example.blogi.core.navigation

object AppDestinations {
    const val HOME = "home"
    const val CREATE = "create"
    const val PROFILE = "profile"

    const val POST_DETAIL = "post_detail/{postId}"

    fun postDetailRoute(postId: Long): String = "post_detail/$postId"
}
```

**Selgitus:**  
`POST_DETAIL` kirjeldab route mustrit, kus `postId` on placeholder.  
`postDetailRoute(postId)` ehitab runtime route väärtuse navigeerimiseks (nt `post_detail/1739383838`).

---

## 2) ViewModelisse postituse leidmise funktsioon

**Fail:**  
`app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`

**Kood:**
```kotlin
fun getPostById(postId: Long): BlogPost? {
    return posts.find { it.id == postId }
}
```

**Selgitus:**  
Detailvaade vajab ühte konkreetset postitust.  
Funktsioon tagastab `BlogPost?`, sest ID ei pruugi alati eksisteerida.

---

## 3) HomeScreen callbacki lisamine

**Fail:**  
`app/src/main/java/com/example/blogi/feature_home/ui/HomeScreen.kt`

### Funktsiooni signatuur

```kotlin
@Composable
fun HomeScreen(
    posts: List<BlogPost>,
    onPostClick: (Long) -> Unit
)
```

### Card klikikäitumine `items(posts...)` plokis

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    onClick = { onPostClick(post.id) },
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
) {
    // olemasolev sisu
}
```

**Selgitus:**  
Home ekraan ei otsusta, kuhu minna.  
Ta saadab ainult sündmuse üles koos vajutatud postituse ID-ga.

---

## 4) AppNavGraph HOME route: callbackist navigeerimine

**Fail:**  
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

**Kood:**
```kotlin
composable(AppDestinations.HOME) {
    HomeScreen(
        posts = blogViewModel.posts,
        onPostClick = { postId ->
            navController.navigate(AppDestinations.postDetailRoute(postId))
        }
    )
}
```

**Selgitus:**  
NavGraph on õige koht route-põhiseks navigeerimiseks.  
`onPostClick` event mapitakse route-stringiks läbi `postDetailRoute(...)`.

---

## 5) Uus detailvaate ekraan

**Fail (uus):**  
`app/src/main/java/com/example/blogi/feature_postdetail/ui/PostDetailScreen.kt`

**Kood:**
```kotlin
package com.example.blogi.feature_postdetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.data.model.BlogPost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun PostDetailScreen(post: BlogPost?) {
    if (post == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Postitust ei leitud",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = formatDate(post.createdAt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

**Selgitus:**  
Ekraan võtab vastu `BlogPost?`.  
Kui postitust ei leita, kuvatakse kontrollitud fallback-olek.

---

## 6) AppNavGraph import detailvaatele

**Fail:**  
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

**Kood:**
```kotlin
import com.example.blogi.feature_postdetail.ui.PostDetailScreen
```

---

## 7) AppNavGraph detail route

**Fail:**  
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

**Kood:**
```kotlin
composable(AppDestinations.POST_DETAIL) { backStackEntry ->
    val postId = backStackEntry.arguments
        ?.getString("postId")
        ?.toLongOrNull()

    val post = postId?.let { blogViewModel.getPostById(it) }

    PostDetailScreen(post = post)
}
```

**Selgitus:**  
Route argumendid loetakse stringina ja teisendatakse `Long`-iks.  
`toLongOrNull()` väldib crashi vigase sisendi korral.

---

## 8) Täiskonteksti näide AppNavGraphis

Alltoodud lõik näitab Home, Create ja Detail route’i koos:

```kotlin
NavHost(
    navController = navController,
    startDestination = AppDestinations.HOME
) {
    composable(AppDestinations.HOME) {
        HomeScreen(
            posts = blogViewModel.posts,
            onPostClick = { postId ->
                navController.navigate(AppDestinations.postDetailRoute(postId))
            }
        )
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
            onDarkThemeChange = onDarkThemeChange
        )
    }
}
```

---

## 9) Tehnilised põhjendused

`onPostClick` callback hoiab Home ekraani navisõltumatuses.  
`postDetailRoute(...)` väldib käsitsi stringi kokku liitmise vigu.  
`getPostById(...)` koondab andmeotsingu ViewModelisse.  
`toLongOrNull()` ja nulli-fallback detailvaates parandavad stabiilsust.

---

## 10) Oodatav tulemus

Pärast sammude rakendamist:
- Home kaardile vajutus avab detailvaate.
- Detailvaade kuvab valitud postituse pealkirja, kuupäeva ja sisu.
- Vigase/mittesobiva ID korral kuvatakse “Postitust ei leitud”.

---

## 11) Tüüpilised vead

`Unresolved reference 'postDetailRoute'`  
Põhjus: funktsioon ei asu `AppDestinations` objekti sees või nimi ei klapi.

`No value passed for parameter 'onPostClick'`  
Põhjus: `HomeScreen` signatuur muutus, kuid kutses callback puudub.

Detail ei avane õigesti  
Põhjus: route muster ja runtime route ei klapi (`POST_DETAIL` vs `postDetailRoute`).

---
