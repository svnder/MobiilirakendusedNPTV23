# Step 3 — Firebase + Auth (login, register, logout)

See dokument katab **ainult** Firebase Authentication osa Android Studios:

- Firebase projekti ühendamine
- `google-services.json`
- Gradle seadistus
- Email/Password sign-in sisselülitamine
- `AuthRepository`
- `AuthViewModel`
- `LoginScreen`
- `RegisterScreen`
- `AuthScreen`
- `logout`

See etapp eeldab, et sinu projekti package on:

```text
com.example.blogi
```

---

## 1. Firebase Console seadistus

### 1.1 Loo või ava Firebase projekt
1. Ava Firebase Console
2. Loo uus projekt või ava olemasolev projekt

### 1.2 Lisa Android app
1. Vali **Add app**
2. Vali **Android**
3. Package name pane täpselt:
   `com.example.blogi`
4. Laadi alla fail:
   `google-services.json`

### 1.3 Pane fail õigesse kohta
Fail peab minema siia:

```text
app/google-services.json
```

Kui `google-services.json` package name ei klapi sinu Android appi `applicationId` väärtusega, siis build ebaõnnestub veaga “No matching client found for package name”. Firebase Android setup juhend nõuab, et registreeritud Android appi package name ja konfiguratsioonifail klapiksid projektiga. citeturn0search0turn0search10

---

## 2. Lülita Email/Password sisse

Firebase Console'is:
1. Ava **Authentication**
2. Ava **Sign-in method**
3. Lülita sisse **Email/Password**
4. Vajuta **Save**

Firebase Auth ei tööta email/parool loginiga enne, kui see provider on Console'is sisse lülitatud. citeturn0search1turn0search6

---

## 3. Gradle seadistus

## 3.1 `app/build.gradle.kts`

**Faili asukoht:**

```text
app/build.gradle.kts
```

### Plugins plokk
Pane sinna see:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}
```

### Dependencies plokk
Pane sinna need read:

```kotlin
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
```

Firebase soovitab Androidis kasutada BoM-i, et Firebase SDK versioonid püsiksid omavahel kooskõlas, ning Authenticationi jaoks lisada `firebase-auth`. Google services plugin ja `google-services.json` on samuti Firebase Android seadistuse osa. citeturn0search0turn0search6turn0search10

> Märkus: Firebase Android BoM saab ajas muutuda; kui tahad hiljem uuendada, kontrolli uusimat BoM versiooni Firebase Android release notes lehelt. citeturn0search2

---

## 4. Kaustastruktuur

Loo need kaustad ja failid:

```text
app/src/main/java/com/example/blogi/feature_auth/
├── data/
│   └── AuthRepository.kt
├── logic/
│   └── AuthViewModel.kt
└── ui/
    ├── AuthScreen.kt
    ├── LoginScreen.kt
    └── RegisterScreen.kt
```

---

## 5. `AuthRepository.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_auth/data/AuthRepository.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.feature_auth.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
```

`signInWithEmailAndPassword`, `createUserWithEmailAndPassword` ja `signOut` on Firebase Authenticationi Android API põhimeetodid email/parool authi jaoks. citeturn0search1turn0search13

---

## 6. `AuthViewModel.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_auth/logic/AuthViewModel.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.feature_auth.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogi.feature_auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loginEmail: String = "",
    val loginPassword: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isLoggedIn = repository.isLoggedIn())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(loginEmail = value)
    }

    fun onLoginPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(loginPassword = value)
    }

    fun onRegisterEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(registerEmail = value)
    }

    fun onRegisterPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(registerPassword = value)
    }

    fun onRegisterConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(registerConfirmPassword = value)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun login() {
        val email = _uiState.value.loginEmail.trim()
        val password = _uiState.value.loginPassword

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sisesta email ja parool"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.login(email, password)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sisselogimine ebaõnnestus"
                )
            }
        }
    }

    fun register() {
        val email = _uiState.value.registerEmail.trim()
        val password = _uiState.value.registerPassword
        val confirmPassword = _uiState.value.registerConfirmPassword

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Täida kõik väljad"
            )
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Parool peab olema vähemalt 6 tähemärki"
            )
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Paroolid ei kattu"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.register(email, password)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Konto loomine ebaõnnestus"
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = _uiState.value.copy(isLoggedIn = false)
    }
}
```

---

## 7. `LoginScreen.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_auth/ui/LoginScreen.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.feature_auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.feature_auth.logic.AuthUiState
import com.example.blogi.ui.components.AppPrimaryButton
import com.example.blogi.ui.components.AppTextField

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Logi sisse",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Sisesta oma konto andmed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppTextField(
            value = uiState.loginEmail,
            onValueChange = onEmailChange,
            placeholder = "Email"
        )

        AppTextField(
            value = uiState.loginPassword,
            onValueChange = onPasswordChange,
            placeholder = "Parool",
            isPassword = true
        )

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        AppPrimaryButton(
            text = if (uiState.isLoading) "Laadib..." else "Logi sisse",
            enabled = !uiState.isLoading,
            onClick = onLoginClick
        )

        TextButton(onClick = onGoToRegister) {
            Text("Pole veel kontot? Registreeru")
        }
    }
}
```

---

## 8. `RegisterScreen.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_auth/ui/RegisterScreen.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.feature_auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.feature_auth.logic.AuthUiState
import com.example.blogi.ui.components.AppPrimaryButton
import com.example.blogi.ui.components.AppTextField

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Loo konto",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Registreeru emaili ja parooliga.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppTextField(
            value = uiState.registerEmail,
            onValueChange = onEmailChange,
            placeholder = "Email"
        )

        AppTextField(
            value = uiState.registerPassword,
            onValueChange = onPasswordChange,
            placeholder = "Parool",
            isPassword = true
        )

        AppTextField(
            value = uiState.registerConfirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Korda parooli",
            isPassword = true
        )

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        AppPrimaryButton(
            text = if (uiState.isLoading) "Laadib..." else "Registreeru",
            enabled = !uiState.isLoading,
            onClick = onRegisterClick
        )

        TextButton(onClick = onGoToLogin) {
            Text("Konto on olemas? Logi sisse")
        }
    }
}
```

---

## 9. `AuthScreen.kt`

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_auth/ui/AuthScreen.kt
```

**Kogu faili sisu:**

```kotlin
package com.example.blogi.feature_auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blogi.feature_auth.logic.AuthViewModel

private enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val authModeState = remember { mutableStateOf(AuthMode.LOGIN) }

    when (authModeState.value) {
        AuthMode.LOGIN -> {
            LoginScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onLoginEmailChange,
                onPasswordChange = authViewModel::onLoginPasswordChange,
                onLoginClick = authViewModel::login,
                onGoToRegister = {
                    authViewModel.clearError()
                    authModeState.value = AuthMode.REGISTER
                }
            )
        }

        AuthMode.REGISTER -> {
            RegisterScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onRegisterEmailChange,
                onPasswordChange = authViewModel::onRegisterPasswordChange,
                onConfirmPasswordChange = authViewModel::onRegisterConfirmPasswordChange,
                onRegisterClick = authViewModel::register,
                onGoToLogin = {
                    authViewModel.clearError()
                    authModeState.value = AuthMode.LOGIN
                }
            )
        }
    }
}
```

---

## 10. `MainActivity.kt` muudatus

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/MainActivity.kt
```

### Lisa import-read

```kotlin
import com.example.blogi.feature_auth.logic.AuthViewModel
import com.example.blogi.feature_auth.ui.AuthScreen
```

### `BlogiTheme { ... }` sisse pane see plokk

```kotlin
val authViewModel: AuthViewModel = viewModel()
val authState by authViewModel.uiState.collectAsState()

if (authState.isLoggedIn) {
    AppEntry(
        darkTheme = darkTheme,
        onDarkThemeChange = { enabled ->
            lifecycleScope.launch {
                themePrefs.setDarkMode(enabled)
            }
        },
        onLogoutClick = {
            authViewModel.logout()
        }
    )
} else {
    AuthScreen(authViewModel = authViewModel)
}
```

---

## 11. `AppEntry(...)` muudatus

**Sama fail:**  
`app/src/main/java/com/example/blogi/MainActivity.kt`

### Funktsiooni päisesse lisa
```kotlin
onLogoutClick: () -> Unit
```

### Näide
```kotlin
@Composable
fun AppEntry(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
)
```

### `AppNavGraph(...)` kutsesse lisa
```kotlin
onLogoutClick = onLogoutClick
```

---

## 12. `AppNavGraph.kt` muudatus

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/core/navigation/AppNavGraph.kt
```

### Funktsiooni päisesse lisa
```kotlin
onLogoutClick: () -> Unit
```

### `ProfileScreen(...)` plokis kasuta
```kotlin
ProfileScreen(
    darkTheme = darkTheme,
    onDarkThemeChange = onDarkThemeChange,
    onLogoutClick = onLogoutClick
)
```

---

## 13. `ProfileScreen.kt` logout

**Faili asukoht:**

```text
app/src/main/java/com/example/blogi/feature_profile/ui/ProfileScreen.kt
```

### Lisa import
```kotlin
import com.example.blogi.ui.components.AppPrimaryButton
```

### Funktsiooni päisesse lisa
```kotlin
onLogoutClick: () -> Unit
```

### `Column { ... }` sisse lisa nupp
```kotlin
AppPrimaryButton(
    text = "Logi välja",
    onClick = onLogoutClick
)
```

---

## 14. Mis peab pärast töötama

Kui kõik on õigesti tehtud:

### Registreerimine
- kasutaja sisestab emaili
- kasutaja sisestab parooli
- kasutaja kordab parooli
- vajutab `Registreeru`
- konto luuakse Firebase Authenticationis
- `isLoggedIn = true`
- kasutaja suunatakse äppi sisse

### Sisselogimine
- kasutaja sisestab emaili
- kasutaja sisestab parooli
- vajutab `Logi sisse`
- Firebase teeb login'i
- `isLoggedIn = true`
- kasutaja suunatakse äppi sisse

### Väljalogimine
- kasutaja läheb profiili
- vajutab `Logi välja`
- `auth.signOut()` käivitatakse
- `isLoggedIn = false`
- kasutaja suunatakse tagasi auth-ekraanile

---

## 15. Levinumad vead

### Viga: `No matching client found for package name`
Põhjus:
- `google-services.json` package name ei klapi
- Firebase Console'is registreeritud Android app kasutab teist package name'i

Parandus:
- registreeri Firebase Console'is Android app package'iga `com.example.blogi`
- laadi uus `google-services.json`
- pane see `app/` kausta

See viga tekib siis, kui Google services plugin ei leia `google-services.json` failist sinu appile sobivat client kirjet. citeturn0search0turn0search10

### Viga: `Redeclaration: AuthViewModel`
Põhjus:
- projektis on kaks `AuthViewModel` klassi

Parandus:
- jäta alles ainult üks `AuthViewModel.kt`

### Viga: komponendid punased
Põhjus:
- vale import
- `com.example.blogiapp` vs `com.example.blogi`

Parandus:
- hoia kõik ühtse juurpakketi all:
  `com.example.blogi`

---

## 16. Kontrollnimekiri

Enne käivitamist kontrolli üle:

- `google-services.json` on `app/` kaustas
- Firebase Console'is on Android app package'iga `com.example.blogi`
- Email/Password provider on sisse lülitatud
- `app/build.gradle.kts` sisaldab Google services pluginat
- `app/build.gradle.kts` sisaldab `firebase-auth`
- `AuthRepository.kt` on olemas
- `AuthViewModel.kt` on olemas
- `LoginScreen.kt` on olemas
- `RegisterScreen.kt` on olemas
- `AuthScreen.kt` on olemas
- `ProfileScreen.kt` sisaldab logout nuppu
- `MainActivity.kt` lülitab `AuthScreen` ja `AppEntry` vahel
- `AppNavGraph.kt` annab `onLogoutClick` edasi

---

## 17. Kokkuvõte

Selles etapis ehitasime Firebase Authentication flow:

- login
- register
- logout

ning ühendasime selle Android Compose appi struktuuriga.

Selles etapis ei käsitletud veel:
- Firestore andmebaasi
- postituste salvestamist serverisse
- kasutajaprofiili andmete hoidmist
- parooli taastamist
