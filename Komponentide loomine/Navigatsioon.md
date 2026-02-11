# Navigatsiooni loomine
## Näidis kuidas luua lihtsat funktsioneerivat navigatsiooni riba


### 1) Alusta tühjast Android Studio Compose projektist

Loo uus projekt:

- **Template**: Empty Activity (Compose)
- **Package name**: `com.example.blogiapp`
- **Language**: Kotlin
- **Min SDK**: näiteks SDK21

- **NB! blogiapp on näidis rakendus, kui teed isikliku tühja projekti siis läheb projekti nimi**

Kui projekt avatud, sul on tavaliselt:
- `MainActivity.kt`
- teema failid (`ui.theme`)

---

## 2) Vajalik dependency navigeerimiseks

Ava `app/build.gradle.kts` ja kontrolli, et olemas oleks Navigation Compose dependency:

```kotlin
dependencies {
    implementation("androidx.navigation:navigation-compose:2.8.0") // või uuem sobiv versioon
}
```

Seejärel:
- **Sync Project with Gradle Files**
- vajadusel **Rebuild Project**

---

## 3) Lõppstruktuur (mida hakkame looma)

Lõpuks peaks `app/src/main/java/com/example/blogiapp` välja nägema nii:

```text
com.example.blogiapp
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

## 4) Sammud täiesti tühjalt: kaustad ja failid

## Samm 4.1 — Loo `core.navigation`

Android Studio puus:

- paremklõps `com.example.blogiapp`
- **New > Package**
- nimi: `core.navigation`

Loo sinna fail: **AppDestinations.kt**

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
## Samm 4.1.1 —  `object` vs `class` Kotlinis

### Väga primitiivne näide:

- `object` on nagu üks karp
- `class` on karbi vorm, millest saab teha mitu karpi

## `object` näidis

```
object AppDestinations {
    const val HOME = "home"
}
```

## Kasutamine

```
AppDestination.HOME
```
### Ehk ei looda uut asja

## `class` näidis

```
class Dog(val name: String)
```

## Kasutamine

```
val d1 = Dog("Muri")
val d2 = Dog("Pontu")

```
### Kaks eri objekti

## Millal kumba valida?

- Kui vaja üksainus koht (nt konstandid) `object`
- Kui vaja mitu eri asja (nt mitu kasutajat/koera/navigatorit) `class`



---

## Samm 4.2 — Loo `AppNavGraph.kt` samasse `core.navigation` paketti

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
NavGraph seob route'id ekraanidega.
startDestination määrab, mis avatakse esimesena.
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

## Samm 4.3 — Loo feature ekraanide kaustad

Loo `com.example.blogiapp` alla järgmised paketid:

- `feature_home.ui`
- `feature_create.ui`
- `feature_profile.ui`

### HomeScreen.kt

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

### CreateScreen.kt

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

### ProfileScreen.kt

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

## Samm 4.4 — Loo bottom bari UI kaust: `feature_navbar.ui`

Loo fail: `AppBottomBar.kt` `feature_navbar.ui`


```kotlin
package com.example.blogiapp.feature_navbar.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.blogiapp.core.navigation.AppDestinations

/* KOMMENTAAR
Bottom bar on eraldi UI komponent.
Ta EI navigeeri ise, vaid saab callbackid parentilt.
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

## Samm 4.5 — Loo bottom bari loogika kaust: `feature_navbar.logic`

Loo fail: `BottomBarNavigator.kt`

```kotlin
package com.example.blogiapp.feature_navbar.logic

import androidx.navigation.NavHostController
import com.example.blogiapp.core.navigation.AppDestinations

/* KOMMENTAAR
Navigeerimise loogika on eraldi klassis.
UI jääb puhtamaks ja testitavamaks.
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

---

## Samm 4.6 — Ühenda kõik `MainActivity.kt`-s

Asenda `MainActivity.kt` sisu (või kohanda) järgmisega:

```kotlin
package com.example.blogi

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
1) navController
2) navigator (eraldi loogikaklass)
3) AppBottomBar (UI)
4) AppNavGraph (route -> screen seos)
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


## 5) Mis asi on mis?

- **AppDestinations**  
  Route stringide keskne koht (`"home"`, `"create"`, `"profile"`)

- **AppNavGraph**  
  Seob route’id Composable ekraanidega

- **Home/Create/ProfileScreen**  
  Feature-ekraanid ehk reaalse UI jaoks

- **AppBottomBar**  
  Alumine naviriba UI ei halda ise navigeerimise reegleid, ainult näitab nuppe ja kutsub callbacke

- **BottomBarNavigator**  
  Navigeerimise loogika klass (`goHome()`, `goCreate()`, `goProfile()`)

- **AppEntry**  
  “Koostefail”: ühendab navController + navGraph + bottombar


## 6) Millal teha uus kaust?


1. **Kui midagi on globaalne Äppi reegel** läheb `core`  
   Näide: `core.navigation`

2. **Kui midagi kuulub ühe konkreetse funktsiooni alla** → `feature_xxx`  
   Näide: `feature_profile.ui`

3. **Kui samas feature’s on nii UI kui loogika** → jaga `ui` ja `logic` alamkaustaks  
   Näide: `feature_navbar.ui` + `feature_navbar.logic`



## Viga: `Unresolved reference 'AppDestinations'`

Kontrolli:

- import olemas:
  ```kotlin
  import com.example.blogiapp.core.navigation.AppDestinations
  ```
- package AppDestinations failis:
  ```kotlin
  package com.example.blogiapp.core.navigation
  ```
- objekti nimi täpselt `AppDestinations`


## 8) Logcat debug (kui nav ei tööta)

Lisa ajutiselt:

```kotlin
import android.util.Log
Log.d("NAV_TEST", "CREATE CLICK")
```

Vaata Android Studio Logcat:

- process: `com.example.blogiapp`
- otsing: `NAV_TEST`

Kui logi ei ilmu, callback ei käivitu või sa ei jõua selle koodiharuni.

---

## 9) Ikoonid navigatsioonile

Kui tahad päris Material ikoone bottombaris:

```kotlin
implementation("androidx.compose.material:material-icons-extended")
```

Pärast:
- Sync
- Rebuild

### Kontrolli oma `MainActivity.kt` faili

```
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
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == AppDestinations.CREATE,
            onClick = onCreateClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Create") },
            label = { Text("Create") }
        )

        NavigationBarItem(
            selected = currentRoute == AppDestinations.PROFILE,
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
```



