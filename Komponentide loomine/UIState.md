# BlogiApp — Samm 5: UI Event Muster (Create voog)
## Ametlik juhend

## Eesmärk

Rakendada Create voos selge UI Event muster, kus:

- `CreateScreen` vastutab ainult kasutajaliidese ja sisendi kogumise eest
- `AppNavGraph` orkestreerib kasutajavoo (salvesta + navigeeri)
- `BlogViewModel` vastutab andmete loogika eest

See eraldus teeb koodi loetavamaks, lihtsamini testitavaks ja paremini laiendatavaks.

---

## Mõisted

### UI State
Andmed, mida ekraan parajasti kuvab (nt `title`, `content`, `posts`).

### UI Event
Kasutaja tegevus, mille UI saadab edasi (nt “Save vajutati”, “postitus klikiti”).

### State down, Events up
- State liigub ülevalt ekraanile
- Event liigub ekraanilt üles loogikakihtidesse

---

## Eeldused

Enne selle sammu alustamist on olemas:

- `BlogViewModel` klass
- `CreateScreen` ekraan
- `AppNavGraph` route'idega `HOME`, `CREATE`, `PROFILE`
- `MainActivity`/`AppEntry`, kus `AppNavGraph` kutsutakse

---

## 1) BlogViewModel: andmete loogika ühes kohas

**Fail:**  
`app/src/main/java/com/example/blogi/feature_blog/logic/BlogViewModel.kt`

### Koodi tükk

```kotlin
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
```

### Selgitus

- `trim()` eemaldab alguse/lõpu tühikud
- `isEmpty()` kontroll väldib tühja postituse salvestamist
- `posts.add(0, ...)` lisab uue postituse listi algusesse (uusim ees)

### Võimalik küsimus

**Miks valida ViewModel, mitte teha see loogika CreateScreenis?**  
Sest ViewModel hoiab andme- ja äriloogika UI-st eraldi. See vähendab segadust ja lihtsustab hilisemaid muudatusi.

---

## 2) CreateScreen: event-põhine UI

**Fail:**  
`app/src/main/java/com/example/blogi/feature_create/ui/CreateScreen.kt`

### 2.1 Funktsiooni signatuur

```kotlin
@Composable
fun CreateScreen(
    onSavePost: (String, String) -> Unit
)
```

### 2.2 UI state read

```kotlin
var title by remember { mutableStateOf("") }
var content by remember { mutableStateOf("") }

val isValid = title.trim().isNotEmpty() && content.trim().isNotEmpty()
```

### 2.3 Save nupu event

```kotlin
Button(
    onClick = {
        onSavePost(title, content)
        title = ""
        content = ""
    },
    enabled = isValid
) {
    Text("Salvesta postitus")
}
```

### Selgitus

- `CreateScreen` kogub sisendi, kuid ei otsusta ise salvestuse implementatsiooni
- `onSavePost(...)` on UI event callback kõrgemale kihile
- `enabled = isValid` annab kohese UX tagasiside
- `isValid` on boolean (true/false) kontrollmuutuja, mis ütleb, kas sisestus on salvestamiseks sobiv.

```
val isValid = title.trim().isNotEmpty() && content.trim().isNotEmpty()
```
- `true` pealkiri ja sisu on mõlemad olemas (pärast tühikute eemaldamist)
- `false` vähemalt üks väli on tühi

```
Button(
    onClick = { ... },
    enabled = isValid
) { ... }
```

### Võimalik küsimus

**Kas `isValid` kontroll peab olema ainult UI-s?**  
UI-s on see kasutajakogemuse jaoks. Andmekihi turvalisuse jaoks peab kontroll olema ka ViewModelis.

---

## 3) AppNavGraph: workflow orkestreerimine

**Fail:**  
`app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`

### 3.1 Funktsiooni parameeter

```kotlin
blogViewModel: BlogViewModel,
```

### 3.2 CREATE route

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

### 3.3 HOME route (et tulemus kohe nähtav oleks)

```kotlin
composable(AppDestinations.HOME) {
    HomeScreen(posts = blogViewModel.posts)
}
```

### Selgitus

- NavGraph koondab route-põhise kasutajavoo
- Save event käivitab kaks sammu:
  1) andmete lisamine ViewModelisse  
  2) navigeerimine Home ekraanile
- `launchSingleTop = true` väldib sama sihtekraani duplikaati topis

### Võimalik küsimus

**Miks navigeerimine siin, mitte CreateScreenis?**  
Et UI jääks tehniliselt neutraalseks ja naviloogika oleks ühes kohas.

---

## 4) AppEntry/MainActivity: ViewModel edasiandmine

**Fail:**  
`app/src/main/java/com/example/blogi/MainActivity.kt`

### 4.1 Importid

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blogi.feature_blog.logic.BlogViewModel
```

### 4.2 AppEntry sees ViewModel loomine

```kotlin
val blogViewModel: BlogViewModel = viewModel()
```

### 4.3 AppNavGraph kutse

```kotlin
AppNavGraph(
    navController = navController,
    darkTheme = darkTheme,
    onDarkThemeChange = onDarkThemeChange,
    blogViewModel = blogViewModel
)
```

### Selgitus

- `viewModel()` tagab ühe jagatud ViewModeli antud nav-host kontekstis
- sama instants antakse Home ja Create voole
- see hoiab andmeallika ühtsena

### Võimalik küsimus

**Miks mitte luua BlogViewModel igas ekraanis eraldi?**  
Siis tekiksid eraldiseisvad state’id ja Create → Home andmevoog katkeks.

---
