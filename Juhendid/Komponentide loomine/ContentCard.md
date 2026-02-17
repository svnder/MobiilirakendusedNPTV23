# Järgmine samm: PostCard + PostList + EmptyState (detailsete koodiselgitustega)

Selles etapis ehitame blogi rakenduse jaoks põhilise sisuvaate enne backendi:
1. `PostCard` komponent
2. `EmptyState` komponent
3. `HomeScreen` list koos otsinguga (fake andmed)

> Eesmärk: UI oleks valmis enne API/Firebase ühendamist.

---

## Millal see samm teha?

Tee see samm **pärast**:
- navigeerimise põhistruktuuri (NavGraph, bottom nav)
- input/search komponentide loomist
- login UI baasi

Tee see samm **enne**:
- päris API ühendus (Retrofit)
- Room cache
- detailvaate andmeallika sidumist

---

## Projekti struktuur (kuhu failid luua)

1. **UUS fail**  
   `app/src/main/java/com/example/blogiapp/feature_common/ui/components/PostCard.kt`

2. **UUS fail**  
   `app/src/main/java/com/example/blogiapp/feature_common/ui/components/EmptyState.kt`

3. **UUENDA olemasolev fail**  
   `app/src/main/java/com/example/blogiapp/feature_home/ui/HomeScreen.kt`

---

## Samm 1 — Loo PostCard.kt

**Asukoht:**  
`feature_common/ui/components/PostCard.kt`

```kotlin
package com.example.blogiapp.feature_common.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/* KOMMENTAAR
@Composable:
- märgib funktsiooni, mis joonistab UI-d.
PostCard:
- üks taaskasutatav UI kaart blogi postituse jaoks.
Parameetrid:
- title/preview/author/date = mida kaardil näidata
- onClick = callback, mida parent ekraan saab kasutada detaili avamiseks
*/
@Composable
fun PostCard(
    title: String,
    preview: String,
    author: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // klikil kutsutakse parentilt saadud callback
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title) // postituse pealkiri
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = preview) // lühikirjeldus
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = author) // vasakul autor
                Text(text = date)   // paremal kuupäev
            }
        }
    }
}
```

### Miks see oluline on?
`PostCard` koondab ühe postituse välimuse ühte kohta.  
Kui disain muutub, muudatus tehakse ühe komponendi sees.

---

## Samm 2 — Loo EmptyState.kt

**Asukoht:**  
`feature_common/ui/components/EmptyState.kt`

```kotlin
package com.example.blogiapp.feature_common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/* KOMMENTAAR
EmptyState:
- üldine komponent olukorraks, kui andmeid pole.
Miks vaja:
- kasutaja saab selge tagasiside, et list pole katki, vaid tühi.
*/
@Composable
fun EmptyState(
    title: String = "Andmeid pole",
    subtitle: String = "Lisa esimene postitus või muuda otsingut."
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center, // vertikaalne keskjoondus
        horizontalAlignment = Alignment.CenterHorizontally // horisontaalne keskjoondus
    ) {
        Text(title)
        Text(subtitle)
    }
}
```

### Miks see oluline on?
Tühi ekraan ilma selgituseta ajab kasutaja segadusse.  
`EmptyState` parandab UX-i ja on kergesti taaskasutatav mujal.

---

## Samm 3 — Uuenda HomeScreen.kt (search + list + empty state)

**Asukoht:**  
`feature_home/ui/HomeScreen.kt`

```kotlin
package com.example.blogiapp.feature_home.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogiapp.feature_common.ui.components.AppSearchBar
import com.example.blogiapp.feature_common.ui.components.EmptyState
import com.example.blogiapp.feature_common.ui.components.PostCard

/* KOMMENTAAR
PostUi:
- lihtne andmeklass HomeScreeni kuvamiseks.
- hiljem saad asendada API/DB mudeliga või mapperiga.
*/
data class PostUi(
    val id: Int,
    val title: String,
    val preview: String,
    val author: String,
    val date: String
)

@Composable
fun HomeScreen() {
    /* KOMMENTAAR
    remember + mutableStateOf:
    - hoiab query väärtust composable elutsüklis.
    - query muutumisel toimub recomposition ja filter arvutatakse uuesti.
    */
    var query by remember { mutableStateOf("") }

    /* KOMMENTAAR
    remember { listOf(...) }:
    - fake andmed luuakse üks kord ja jäävad stabiilseks recompositionite vahel.
    */
    val allPosts = remember {
        listOf(
            PostUi(1, "Compose algus", "Kuidas alustada Jetpack Compose'iga...", "Mari", "2026-02-11"),
            PostUi(2, "Navigation puhtalt", "Kuidas hoida nav loogika eraldi...", "Karl", "2026-02-10"),
            PostUi(3, "StateFlow praktikad", "Lihtsad mustrid algajale...", "Anna", "2026-02-08")
        )
    }

    /* KOMMENTAAR
    Filter:
    - otsib title/preview/author väljadest.
    - ignoreCase = true teeb otsingu kasutajasõbralikumaks.
    */
    val filteredPosts = allPosts.filter {
        it.title.contains(query, ignoreCase = true) ||
        it.preview.contains(query, ignoreCase = true) ||
        it.author.contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSearchBar(
            query = query,
            onQueryChange = { query = it }, // igal sisendimuutusel uuendame state'i
            label = "Otsi postitusi"
        )

        if (filteredPosts.isEmpty()) {
            // Kui filter ei leia vasteid, näitame EmptyState'i
            EmptyState(
                title = "Postitusi ei leitud",
                subtitle = "Proovi teist märksõna."
            )
        } else {
            /* KOMMENTAAR
            LazyColumn:
            - efektiivne list, renderdab ainult nähtavad read.
            - sobib pikkade listide jaoks.
            */
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredPosts) { post ->
                    PostCard(
                        title = post.title,
                        preview = post.preview,
                        author = post.author,
                        date = post.date,
                        onClick = {
                            // ajutine test: detaili asemel logime clicki
                            Log.d("POST_UI", "Clicked post id=${post.id}")
                        }
                    )
                }
            }
        }
    }
}
```

### Miks see oluline on?
- Otsing + list + empty state on peaaegu iga sisurakenduse standard.
- Saad sama struktuuri hiljem siduda ViewModeli, API ja Roomiga.

---

## Oodatud tulemus

1. Home ekraanil on otsinguriba.
2. Kuvatakse postituste list kaardina.
3. Otsing filtreerib nimekirja reaalajas.
4. Kui vasteid pole, kuvatakse EmptyState.

---

## Tüüpilised vead ja kontroll

1. **Import puudub**
   - Lahendus: `Alt+Enter` auto-import.

2. **`AppSearchBar` ei leita**
   - Kontrolli, et `CommonInputs.kt` on package’is  
     `feature_common.ui.components`.

3. **HomeScreen ei avane**
   - Kontrolli NavGraph route sidumist (`HOME -> HomeScreen()`).

4. **Build error pärast failide lisamist**
   - `Sync Project with Gradle Files`
   - `Build > Rebuild Project`

---

## Kiire kontrollnimekiri

- [ ] `PostCard.kt` loodud õiges kaustas  
- [ ] `EmptyState.kt` loodud õiges kaustas  
- [ ] `HomeScreen.kt` uuendatud  
- [ ] Search töötab  
- [ ] EmptyState kuvatakse, kui vasteid pole  
- [ ] PostCard click logib Logcati

---

## Järgmine soovituslik samm

**Post detail screen + route argument (`postId`)**,  
et kaardi klikist avaneks eraldi detailvaade.
