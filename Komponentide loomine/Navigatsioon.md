# BlogiApp — toores Markdown dokumentatsioon
## Navigation teekond: algusest kuni Samm 9 (koos vigade ja parandustega)

---

## 0) Lähteolukord

- Sul oli Android Studio Compose projekt, tühi algus (“Hello Android”).
- Põhipackage: `com.example.blogiapp`.
- Eesmärk: ehitada navigeerimine puhtalt, samm-sammult, õpetamiseks sobiva struktuuriga.

---

## 1) Kokkulepitud tööstiil

- Liigume väikeste sammudega.
- Ei viska kogu arhitektuuri kohe peale.
- Kasutame kommentaariplokke:

```kotlin
/* KOMMENTAAR
Mida see teeb?
Miks see vajalik on?
*/
```

- Eesmärk oli: naviriba lõpuks eraldi, naviloogika eraldi, route’id ühes kohas.

---

## 2) Samm 1 — `core.navigation` paketi loomine

### Kuhu kaust luua?
- Android Studio puus:
  - `app > kotlin+java > com.example.blogiapp`
- Paremklõps `com.example.blogiapp` peal → **New > Package**.

### Mis pakett luua?
- `core.navigation`

### Tulem
- `com.example.blogiapp.core.navigation` olemas.

---

## 3) Samm 2 — `AppDestinations.kt` loomine

### Kuhu fail luua?
- `app > kotlin+java > com.example.blogiapp > core.navigation`
- New > Kotlin Class/File → `AppDestinations.kt`

### Kood
```kotlin
package com.example.blogiapp.core.navigation

/* KOMMENTAAR
Siin hoiame kõik route nimed ühes kohas.
Nii väldime kirjavigu navigeerimisel.
*/
object AppDestinations {
    const val HOME = "home"
    const val CREATE = "create"
    const val PROFILE = "profile"
}
```

### Sul tekkinud probleem
`AppDestinations.kt` faili oli jäänud:
- `@Composable`
- `import androidx.compose.runtime.Composable`

See tekitas vea, sest tegemist pole UI composable funktsiooniga.

### Parandus
- Eemalda `@Composable`
- Eemalda Compose import
- Jäta ainult `object AppDestinations`.

---

## 4) Samm 3 — `AppNavGraph.kt` loomine

### Kuhu fail luua?
- `app > kotlin+java > com.example.blogiapp > core.navigation`
- New > Kotlin Class/File → `AppNavGraph.kt`

### Kood (versioon, mida lõpuks kasutasid)
```kotlin
package com.example.blogiapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogiapp.feature_create.ui.CreateScreen
import com.example.blogiapp.feature_home.ui.HomeScreen
import com.example.blogiapp.feature_profile.ui.ProfileScreen

/* KOMMENTAAR
NavGraph seob route'id eraldi screenidega.
Navigation loogika on nüüd puhas ja eraldatud UI failidest.
*/
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) { HomeScreen() }
        composable(AppDestinations.CREATE) { CreateScreen() }
        composable(AppDestinations.PROFILE) { ProfileScreen() }
    }
}
```

---

## 5) Samm 4 — `MainActivity` sisse lihtne navibaasi UI

Alguses panime kõik samasse faili, et kiiresti tööle saada:
- `Scaffold`
- `SimpleBottomBar`
- `AppNavGraph`

### Näidiskood (vaheversioon)
```kotlin
@Composable
fun AppEntry() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            SimpleBottomBar()
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(navController = navController)
        }
    }
}
```

---

## 6) Samm 5 — callbackid naviriba nuppudele

### Eesmärk
Et `SimpleBottomBar` ei oleks “tühi nuppudega UI”, vaid saaks klikid parentilt kaasa.

### `SimpleBottomBar` signatuur
```kotlin
@Composable
fun SimpleBottomBar(
    onHomeClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit
) { ... }
```

### `AppEntry` kutse
```kotlin
SimpleBottomBar(
    onHomeClick = {
        navController.navigate(AppDestinations.HOME) {
            launchSingleTop = true
        }
    },
    onCreateClick = {
        navController.navigate(AppDestinations.CREATE) {
            launchSingleTop = true
        }
    },
    onProfileClick = {
        navController.navigate(AppDestinations.PROFILE) {
            launchSingleTop = true
        }
    }
)
```

### Sul tekkinud probleem
`No parameter with name 'onProfileClick' found.`

### Põhjus
`SimpleBottomBar` funktsioonis puudus `onProfileClick` parameeter või signatuur ei klappinud.

### Parandus
Funktsiooni signatuur viia täpselt kooskõlla kutsega.

---

## 7) Samm 6 — `Unit` vs `unit` probleem

### Sul tekkinud probleem
`Unresolved reference 'unit'`

### Põhjus
Kirjutati väikese tähega `unit`.

### Parandus
Kotlinis peab olema:
```kotlin
() -> Unit
```
mitte
```kotlin
() -> unit
```

---

## 8) Samm 7 — `AppDestinations` unresolved reference

### Sul tekkinud probleem
`Unresolved reference 'AppDestinations'`

### Kontrollnimekiri
1. `MainActivity` (või AppRoot faili) import:
```kotlin
import com.example.blogiapp.core.navigation.AppDestinations
```

2. `AppDestinations.kt` package peab olema:
```kotlin
package com.example.blogiapp.core.navigation
```

3. Objekti nimi peab klappima:
```kotlin
object AppDestinations
```

---

## 9) Samm 8 — aktiivse tabi loogika (`selected`)

### Eesmärk
Bottom bar näitaks, milline tab on aktiivne.

### Vajalikud importid
```kotlin
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
```

### `AppEntry` sees
```kotlin
val backStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = backStackEntry?.destination?.route
```

### `SimpleBottomBar` signatuur
```kotlin
fun SimpleBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit
)
```

### `selected` read
```kotlin
selected = currentRoute == AppDestinations.HOME
selected = currentRoute == AppDestinations.CREATE
selected = currentRoute == AppDestinations.PROFILE
```

---

## 10) Samm 9 — miks ekraan ei vahetanud, kuidas lahendasime

Sul oli olukord, kus klikid ei andnud tulemust.

### Mis osutus praktiliseks kontrolliks
- Tegime callbackide kontrolli lihtsa lähenemisega.
- Peamine probleem oli callbackide/sulgude/signatuuri ebakõla.

### Sul tekkinud konkreetne viga
- Lisasulg (`}`) callbackis katkestas struktuuri.
- Tulemuseks callback ei töötanud korrektselt.

### Õige `Scaffold(bottomBar = ...)` plokk
```kotlin
Scaffold(
    bottomBar = {
        SimpleBottomBar(
            currentRoute = currentRoute,
            onHomeClick = {
                navController.navigate(AppDestinations.HOME) {
                    launchSingleTop = true
                }
            },
            onCreateClick = {
                navController.navigate(AppDestinations.CREATE) {
                    launchSingleTop = true
                }
            },
            onProfileClick = {
                navController.navigate(AppDestinations.PROFILE) {
                    launchSingleTop = true
                }
            }
        )
    }
) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
        AppNavGraph(navController = navController)
    }
}
```

---

## 11) Logcat kontroll (tegime ka selle läbi)

Sa küsisid, kuhu `NAV_TEST` kirjutada.

### Vastus
`NAV_TEST` on logi tag, mis läheb koodi sisse:
```kotlin
import android.util.Log

Log.d("NAV_TEST", "CREATE CLICK")
```

### Kust näha?
- Android Studio → **Logcat** aken
- Filter: app process `com.example.blogiapp`
- Otsing: `NAV_TEST`

### Sul oli segadus
Logcatis oli süsteemi warning, mitte sinu logi.

### Järeldus
Kui `NAV_TEST` ei ilmu, callback ei käivitu või kood pole selles harus.

---

## 12) Importide teema (sinu küsimus: kas kirjutada enne või pärast)

### Soovitus
Kõige mõistlikum Android Studios:
1. kirjuta kood,
2. siis lase IDE-l importida (Alt+Enter),
3. vajadusel Optimize Imports.

### Sul tekkinud probleem
Shortcut `Optimize Imports` ei töötanud.

### Parandus
- kasuta `Alt+Enter` punase sümboli peal
- vajadusel import käsitsi
- Sync/Rebuild, kui IDE jääb toppama

---

## 13) Feature-kaustadesse tõstmine (navigation puhastus)

Hiljem tõstsime ekraanid eraldi feature-kaustadesse.

### Kuhu kaustad luua?
- `app > kotlin+java > com.example.blogiapp`
- New > Package

Loodud paketid:
- `feature_home.ui`
- `feature_create.ui`
- `feature_profile.ui`

### Failid ja kood

#### `feature_home/ui/HomeScreen.kt`
```kotlin
package com.example.blogiapp.feature_home.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/* KOMMENTAAR
Home ekraan eraldi feature kaustas.
*/
@Composable
fun HomeScreen() {
    Text("Home Screen")
}
```

#### `feature_create/ui/CreateScreen.kt`
```kotlin
package com.example.blogiapp.feature_create.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/* KOMMENTAAR
Create ekraan eraldi feature kaustas.
*/
@Composable
fun CreateScreen() {
    Text("Create Screen")
}
```

#### `feature_profile/ui/ProfileScreen.kt`
```kotlin
package com.example.blogiapp.feature_profile.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/* KOMMENTAAR
Profile ekraan eraldi feature kaustas.
*/
@Composable
fun ProfileScreen() {
    Text("Profile Screen")
}
```

---

## 14) Navbari eraldamine oma kausta

### Kuhu kaust luua?
- `app > kotlin+java > com.example.blogiapp`
- New > Package: `feature_navbar.ui`

### Fail
- `feature_navbar/ui/AppBottomBar.kt`

### Kood (ilma forEach-ta, nagu soovisid)
```kotlin
package com.example.blogiapp.feature_navbar.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.blogiapp.core.navigation.AppDestinations

/* KOMMENTAAR
Navbar teadlikult ilma forEach'ita.
Iga nupp on eraldi kirjas, et algajal oleks lihtsam lugeda.
*/
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == AppDestinations.HOME,
            onClick = onHomeClick,
            icon = { Text("•") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == AppDestinations.CREATE,
            onClick = onCreateClick,
            icon = { Text("•") },
            label = { Text("Create") }
        )

        NavigationBarItem(
            selected = currentRoute == AppDestinations.PROFILE,
            onClick = onProfileClick,
            icon = { Text("•") },
            label = { Text("Profile") }
        )
    }
}
```

---

## 15) Samm 9 lõpetamine — navigeerimisloogika eraldi klassi (`feature_navbar.logic`)

### Kuhu kaust luua?
- `app > kotlin+java > com.example.blogiapp`
- New > Package: `feature_navbar.logic`

### Fail
- `feature_navbar/logic/BottomBarNavigator.kt`

### Kood
```kotlin
package com.example.blogiapp.feature_navbar.logic

import androidx.navigation.NavHostController
import com.example.blogiapp.core.navigation.AppDestinations

/* KOMMENTAAR
Navigeerimise loogika on eraldi klassis.
UI jääb puhtamaks.
*/
class BottomBarNavigator(
    private val navController: NavHostController
) {
    fun goHome() {
        navController.navigate(AppDestinations.HOME) {
            launchSingleTop = true
        }
    }

    fun goCreate() {
        navController.navigate(AppDestinations.CREATE) {
            launchSingleTop = true
        }
    }

    fun goProfile() {
        navController.navigate(AppDestinations.PROFILE) {
            launchSingleTop = true
        }
    }
}
```

### AppRoot/AppEntry kasutus
```kotlin
val navigator = remember(navController) { BottomBarNavigator(navController) }

AppBottomBar(
    currentRoute = currentRoute,
    onHomeClick = { navigator.goHome() },
    onCreateClick = { navigator.goCreate() },
    onProfileClick = { navigator.goProfile() }
)
```

---

## 16) Sinu konkreetsed probleemid ja lahendused (koond)

1. **`AppNavGraph` unresolved reference**
   - Kontrolli importi ja package’i.
   - Veendu, et fail on `main` sourceSetis, mitte `test/androidTest`.

2. **`No parameter with name 'onProfileClick' found`**
   - `SimpleBottomBar` signatuur ei klappinud kutsega.
   - Lisa puuduv parameeter.

3. **`Unresolved reference 'unit'`**
   - Kotlinis `Unit` (suur U), mitte `unit`.

4. **`AppDestinations` unresolved reference**
   - vale/missing import
   - vale objekti nimi
   - vale package `AppDestinations.kt` failis

5. **Nav ei vaheta ekraani**
   - kontrolli, et `onClick = onCreateClick` jne (mitte `{ }`)
   - kontrolli sulgude tasakaalu callbackides
   - `AppNavGraph` peab saama sama `navController` eksemplari

6. **Logcat segadus**
   - `NAV_TEST` tuleb kirjutada `Log.d("NAV_TEST", "...")` sisse
   - Logcat filter: protsess `com.example.blogiapp`, query `NAV_TEST`

7. **Optimize Imports shortcut ei toiminud**
   - kasuta `Alt+Enter` quick fix
   - vajadusel import käsitsi
   - Sync/Rebuild

---

## 17) Soovitatud kaustapuu pärast Samm 9

```text
app/src/main/java/com/example/blogiapp
├─ core
│  └─ navigation
│     ├─ AppDestinations.kt
│     └─ AppNavGraph.kt
├─ feature_navbar
│  ├─ logic
│  │  └─ BottomBarNavigator.kt
│  └─ ui
│     └─ AppBottomBar.kt
├─ feature_home
│  └─ ui
│     └─ HomeScreen.kt
├─ feature_create
│  └─ ui
│     └─ CreateScreen.kt
├─ feature_profile
│  └─ ui
│     └─ ProfileScreen.kt
└─ MainActivity.kt
```

---

## 18) Täielik tööversiooni näide (Samm 9 tasemel)

```kotlin
package com.example.blogiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.blogiapp.core.navigation.AppNavGraph
import com.example.blogiapp.feature_navbar.logic.BottomBarNavigator
import com.example.blogiapp.feature_navbar.ui.AppBottomBar
import com.example.blogiapp.ui.theme.BlogiAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlogiAppTheme {
                AppEntry()
            }
        }
    }
}

/* KOMMENTAAR
AppEntry seob kokku:
- navController
- navigator (eraldi loogikaklass)
- AppBottomBar
- AppNavGraph
*/
@Composable
fun AppEntry() {
    val navController = rememberNavController()
    val navigator = remember(navController) { BottomBarNavigator(navController) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = currentRoute,
                onHomeClick = { navigator.goHome() },
                onCreateClick = { navigator.goCreate() },
                onProfileClick = { navigator.goProfile() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(navController = navController)
        }
    }
}
```

---

## 19) Märkus ikoonide kohta (järgmine samm pärast Samm 9)

Kui lisad Material ikoonid ja version-catalog alias ei tööta, kasuta otse dependency stringi:

```kotlin
implementation("androidx.compose.material:material-icons-extended")
```

Pärast:
- Sync Project with Gradle Files
- Rebuild Project

---
