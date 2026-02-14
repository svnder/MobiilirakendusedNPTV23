# BlogiApp — Samm 3 täiendus: Dark Mode salvestamine olemasoleva koodi peale

## Muudatuste eesmärk

Täiendada olemasolevat töötavat dark mode lahendust nii, et valik salvestub ja taastub rakenduse uuesti avamisel.

---

## 1) Dependency lisamine

**Fail:** `app/build.gradle.kts`  
**Asukoht:** `dependencies { ... }`

Lisa:

```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

---

## 2) Uus kaust ja fail

**Kaust (loo kui puudub):**

```text
app/src/main/java/com/example/blogi/core/settings
```

**Fail:**

```text
app/src/main/java/com/example/blogi/core/settings/ThemePreferences.kt
```

**Faili sisu:**

```kotlin
package com.example.blogi.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode_enabled")

    val darkModeFlow = context.dataStore.data.map { prefs ->
        prefs[darkModeKey] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[darkModeKey] = enabled
        }
    }
}
```

---

## 3) MainActivity importide täiendamine

**Fail:** `app/src/main/java/com/example/blogi/MainActivity.kt`

Lisa importid:

```kotlin
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.blogi.core.settings.ThemePreferences
import kotlinx.coroutines.launch
```

---

## 4) MainActivity `setContent {}` sees darkTheme allika asendamine

**Fail:** `app/src/main/java/com/example/blogi/MainActivity.kt`  
**Asukoht:** `setContent { ... }`

### Asenda olemasolev rida

Vana:
```kotlin
var darkTheme by rememberSaveable { mutableStateOf(false) }
```

Uus:
```kotlin
val context = LocalContext.current
val themePrefs = remember { ThemePreferences(context) }
val darkTheme by themePrefs.darkModeFlow.collectAsState(initial = false)
```

---

## 5) MainActivity `AppEntry(...)` callbacki asendamine

**Fail:** `app/src/main/java/com/example/blogi/MainActivity.kt`  
**Asukoht:** `AppEntry(...)` väljakutse

### Asenda callback

Vana:
```kotlin
onDarkThemeChange = { darkTheme = it }
```

Uus:
```kotlin
onDarkThemeChange = { enabled ->
    lifecycleScope.launch {
        themePrefs.setDarkMode(enabled)
    }
}
```

---

## 6) Mida ei muudeta

Järgmised olemasolevad osad jäävad samaks:

- `AppNavGraph` signatuur ja Profile route ühendus
- `ProfileScreen` switch
- `BlogiTheme(darkTheme = darkTheme, ...)`

---

## 7) Tulemus

Dark mode valik salvestub DataStore'i ja rakenduse uuesti avamisel taastub automaatselt.
