# BlogiApp — Dark Mode ainult Profile ekraanilt (Step-by-step)

## Eesmärk

Rakenduses on navigeerimisriba juba olemas.  
Selles juhendis lisatakse **Dark Mode lüliti ainult Profile ekraanile**, nii et lüliti muudab **kogu rakenduse teemat**.

---

## Eeldused

- Projekt kasutab Jetpack Compose + Navigation Compose.
- Baaspakett: `com.example.blogi`
- Olemas on:
  - `MainActivity.kt`
  - `AppBottomBar`
  - `BottomBarNavigator`
  - `AppDestinations`
  - `AppNavGraph`
  - `HomeScreen`, `CreateScreen`, `ProfileScreen`
  - `BlogiTheme` (`ui.theme/Theme.kt`)

---

## 1. AppNavGraph uuendamine

**Fail:** `app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt`  
**Tegevus:** asenda faili sisu

```kotlin
package com.example.blogi.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogi.feature_create.ui.CreateScreen
import com.example.blogi.feature_home.ui.HomeScreen
import com.example.blogi.feature_profile.ui.ProfileScreen

/* KOMMENTAAR
NavGraph + Dark mode state liigutamine ProfileScreeni kaudu.
*/
@Composable
fun AppNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) { HomeScreen() }
        composable(AppDestinations.CREATE) { CreateScreen() }

        /* KOMMENTAAR
        Darkmode koodi tükk
        Profile saab darkTheme väärtuse ja callbacki.
        */
        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange
            )
        }
    }
}
```

---

## 2. ProfileScreen uuendamine

**Fail:** `app/src/main/java/com/example/blogi/feature_profile/ui/ProfileScreen.kt`  
**Tegevus:** asenda faili sisu

```kotlin
package com.example.blogi.feature_profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/* KOMMENTAAR
Profile ekraanil on ainus switch, mis muudab kogu appi teemat.
*/
@Composable
fun ProfileScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile Screen",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        /* KOMMENTAAR
        Darkmode koodi tükk
        Näitab ON/OFF staatust.
        */
        Text(
            text = if (darkTheme) "Dark mode: ON" else "Dark mode: OFF",
            color = MaterialTheme.colorScheme.onBackground
        )

        /* KOMMENTAAR
        Darkmode koodi tükk
        Switch muudab teema väärtust.
        */
        Switch(
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange
        )
    }
}
```

---

## 3. MainActivity uuendamine

**Fail:** `app/src/main/java/com/example/blogi/MainActivity.kt`  
**Tegevus:** asenda faili sisu

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.blogi.core.navigation.AppNavGraph
import com.example.blogi.feature_navbar.logic.BottomBarNavigator
import com.example.blogi.feature_navbar.ui.AppBottomBar
import com.example.blogi.ui.theme.BlogiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            /* KOMMENTAAR
            Darkmode koodi tükk
            Kogu appi teema state.
            */
            var darkTheme by rememberSaveable { mutableStateOf(false) }

            /* KOMMENTAAR
            Darkmode koodi tükk
            BlogiTheme saab darkTheme väärtuse.
            */
            BlogiTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                AppEntry(
                    darkTheme = darkTheme,
                    onDarkThemeChange = { darkTheme = it }
                )
            }
        }
    }
}

/* KOMMENTAAR
AppEntry seob:
- navController
- bottom bar
- nav graph
- dark mode state forwarding
*/
@Composable
fun AppEntry(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
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
            /* KOMMENTAAR
            Darkmode koodi tükk
            NavGraph saab darkTheme ja callbacki, et anda need edasi ProfileScreenile.
            */
            AppNavGraph(
                navController = navController,
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange
            )
        }
    }
}
```

---

## 4. Theme signatuuri kontroll

**Fail:** `app/src/main/java/com/example/blogi/ui/theme/Theme.kt`  
**Kontroll:** `BlogiTheme` funktsiooni signatuur

```kotlin
@Composable
fun BlogiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
```

Kui signatuur on olemas, täiendavaid muudatusi pole vaja.

---

## 5. Build ja käivitamine

1. **Sync Project with Gradle Files**
2. **Rebuild Project**
3. Käivita rakendus

---

## 6. Oodatav tulemus

- Switch asub ainult **Profile** ekraanil.
- Switch muudab `darkTheme` väärtust.
- `BlogiTheme(darkTheme = ...)` tõttu muutub kogu rakenduse värviskeem.

---

## 7. Veakontrolli nimekiri

1. Kõik package nimed kasutavad sama baasnime: `com.example.blogi...`
2. `AppNavGraph(...)` signatuur klapib `MainActivity` väljakutsega
3. `ProfileScreen(...)` signatuur klapib `AppNavGraph` väljakutsega
4. `BlogiTheme` import on korrektne:

```kotlin
import com.example.blogi.ui.theme.BlogiTheme
```
