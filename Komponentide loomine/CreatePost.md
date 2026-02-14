# BlogiApp — Postituse koostamise dokumentatsioon
## Samm 4: Create → Home (mälus, ilma andmebaasita)

## Eesmärk

Lisada rakendusse postituse koostamise voog:

- Create ekraanil sisestatakse pealkiri ja sisu
- Salvestamisel lisatakse postitus ühisesse state'i
- Home ekraanil kuvatakse postituste list

---

## Eeldused

- Navigeerimine töötab (`Home`, `Create`, `Profile`)
- Dark mode ja dark mode salvestus on juba olemas
- Baaspakett: `com.example.blogi`

---

## 1) Postituse mudel

**Fail (uus):**  
`app/src/main/java/com/example/blogi/data/model/BlogPost.kt`

```kotlin
package com.example.blogi.data.model

data class BlogPost(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long
)
```

---

## 2) BlogViewModel postituste hoidmiseks

**Fail (uus):**  
`app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`

```kotlin
package com.example.blogi.feature_blog.logic

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.blogi.data.model.BlogPost

class BlogViewModel : ViewModel() {

    val posts = mutableStateListOf<BlogPost>()

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

## 3) MainActivity/AppEntry: ViewModel loomine ja edasiandmine

**Fail:**  
`app/src/main/java/com/example/blogi/MainActivity.kt`

### 3.1 Importid (lisa)

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blogi.feature_blog.logic.BlogViewModel
```

### 3.2 `AppEntry(...)` sees ViewModel (lisa)

```kotlin
val blogViewModel: BlogViewModel = viewModel()
```

### 3.3 `AppNavGraph(...)` väljakutse argument (lisa)

```kotlin
blogViewModel = blogViewModel
```

---

## 4) AppNavGraph: Home/Create ühendamine ViewModeliga

**Fail:**  
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

### 4.1 Import (lisa)

```kotlin
import com.example.blogi.feature_blog.logic.BlogViewModel
```

### 4.2 Funktsiooni parameeter (lisa)

```kotlin
blogViewModel: BlogViewModel,
```

### 4.3 HOME route plokk (muuda)

```kotlin
composable(AppDestinations.HOME) {
    HomeScreen(posts = blogViewModel.posts)
}
```

### 4.4 CREATE route plokk (muuda)

```kotlin
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
```

---

## 5) CreateScreen: sisestus + salvestus callback

**Fail:**  
`app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt`

### 5.1 Importid (lisa)

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
```

### 5.2 Funktsiooni signatuur (muuda)

```kotlin
@Composable
fun CreateScreen(
    onSavePost: (String, String) -> Unit
)
```

### 5.3 State read (lisa funktsiooni alguses)

```kotlin
var title by remember { mutableStateOf("") }
var content by remember { mutableStateOf("") }

val titleCount = title.length
val contentCount = content.length
val isValid = title.trim().isNotEmpty() && content.trim().isNotEmpty()
```

### 5.4 Layout plokk (lisa/uuenda funktsiooni sees)

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
) {
    Text(
        text = "Uus postitus",
        style = MaterialTheme.typography.headlineSmall
    )

    Text(
        text = "Kirjuta pealkiri ja sisu. Vajuta Salvesta, et postitus lisada avalehele.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    HorizontalDivider()

    OutlinedTextField(
        value = title,
        onValueChange = { if (it.length <= 80) title = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Pealkiri") },
        placeholder = { Text("Nt: Kuidas õppida Jetpack Compose'i") },
        singleLine = true,
        supportingText = { Text("$titleCount / 80") }
    )

    OutlinedTextField(
        value = content,
        onValueChange = { if (it.length <= 2000) content = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        label = { Text("Sisu") },
        placeholder = { Text("Kirjuta postituse sisu siia...") },
        minLines = 8,
        maxLines = 16,
        singleLine = false,
        supportingText = { Text("$contentCount / 2000") }
    )

    Button(
        onClick = {
            onSavePost(title, content)
            title = ""
            content = ""
        },
        enabled = isValid,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Salvesta postitus")
    }
}
```

---

## 6) HomeScreen: postituste list + empty state + preview

**Fail:**  
`app/src/main/java/com/example/blogi/feature_home/ui/HomeScreen.kt`

### 6.1 Importid (lisa)

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.data.model.BlogPost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
```

### 6.2 Helper funktsioon (lisa faili)

```kotlin
private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
```

### 6.3 Funktsiooni signatuur (muuda)

```kotlin
@Composable
fun HomeScreen(
    posts: List<BlogPost>
)
```

### 6.4 Empty state plokk (üles joondatud)

```kotlin
if (posts.isEmpty()) {
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
```

### 6.5 Listi plokk (kui postitused olemas)

```kotlin
else {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
    }
}
```

---

## 7) Tulemus

- Create ekraanil saab sisestada pealkirja ja sisu
- Salvesta lisab postituse ja navigeerib Home ekraanile
- Home näitab postitusi kaardina
- Kui postitusi pole, kuvatakse ülaservas empty state
- Sisu kuvatakse preview kujul

---

## Märkus

Selles sammus hoitakse andmeid mälus (`ViewModel` + `mutableStateListOf`).  
Rakenduse sulgemisel postitused kaovad. Püsisalvestus lisatakse järgmises sammus.
