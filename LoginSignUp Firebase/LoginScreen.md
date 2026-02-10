# LoginScreen navigeerimise samm — selgitustega

Selles etapis tegime LoginScreeni nähtavaks nii, et rakendus avaneb esmalt login vaatega.  
See on oluline, sest enne backend/auth ühendamist saame kogu navigeerimise loogika stabiilselt paika.

---

## Miks see samm on oluline?

Kui `startDestination` on õigesti paigas ja route’id korrektsed, on kogu rakenduse “sissepääsu” loogika kontrollitav ühest kohast.  
Hiljem, kui lisad Firebase/Auth-i, saad sama navigeerimisstruktuuri edasi kasutada ilma suure ümbertegemiseta.

---

## 1) AppDestinations — route’id ühes kohas

Fail: `core/navigation/AppDestinations.kt`

```kotlin
package com.example.blogiapp.core.navigation

object AppDestinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CREATE = "create"
    const val PROFILE = "profile"
}
```

**Selgitus:**  
Route stringid hoiame ühes objektis, et vältida kirjavigu (`"login"` vs `"Login"` jne).  
Kui route’id on laiali eri failides, tekib kiiresti “miks navigeerimine ei tööta” tüüpi vigu.

---

## 2) LoginScreen — eraldi feature kaustas

Fail: `feature_auth/ui/LoginScreen.kt`

```kotlin
package com.example.blogiapp.feature_auth.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogiapp.feature_common.ui.components.AppPasswordInput
import com.example.blogiapp.feature_common.ui.components.AppPrimaryButton
import com.example.blogiapp.feature_common.ui.components.AppTextInputWithError

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isEmailError = email.isNotBlank() && !email.contains("@")
    val isPasswordError = password.isNotBlank() && password.length < 6
    val formValid = email.contains("@") && password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Login")

        AppTextInputWithError(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            isError = isEmailError,
            errorText = "Email peab sisaldama @ märki"
        )

        AppPasswordInput(
            value = password,
            onValueChange = { password = it },
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )

        if (isPasswordError) {
            Text("Parool peab olema vähemalt 6 tähemärki")
        }

        AppPrimaryButton(
            text = "Logi sisse",
            enabled = formValid,
            onClick = {
                Log.d("LOGIN_UI", "Login click: $email")
                onLoginClick(email, password)
            }
        )
    }
}
```

**Selgitus:**  
- `onLoginClick` callback hoiab LoginScreeni “UI-kihina” — ekraan ei otsusta ise kuhu navigeerida.  
- `formValid` abil kontrollid nuppu enne backendi kõnet; kasutaja ei saa vigase sisendiga edasi minna.

---

## 3) AppNavGraph — Login esimeseks ekraaniks

Fail: `core/navigation/AppNavGraph.kt`

```kotlin
package com.example.blogiapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogiapp.feature_auth.ui.LoginScreen
import com.example.blogiapp.feature_create.ui.CreateScreen
import com.example.blogiapp.feature_home.ui.HomeScreen
import com.example.blogiapp.feature_profile.ui.ProfileScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN
    ) {
        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginClick = { _, _ ->
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.HOME) { HomeScreen() }
        composable(AppDestinations.CREATE) { CreateScreen() }
        composable(AppDestinations.PROFILE) { ProfileScreen() }
    }
}
```

**Selgitus (oluline):**
- `startDestination = AppDestinations.LOGIN` määrab, milline ekraan avaneb rakenduse käivitamisel.
- `popUpTo(LOGIN) { inclusive = true }` eemaldab login ekraani back stackist, et Back nupuga ei mindaks pärast sisselogimist tagasi loginisse.

---

## 4) AppEntry / MainActivity kontroll

`AppEntry` peab kasutama ühte `navController` instantsi ja andma selle `AppNavGraph`-ile.

```kotlin
@Composable
fun AppEntry() {
    val navController = rememberNavController()
    AppNavGraph(navController = navController)
}
```

**Selgitus:**  
Kui kasutad kogemata mitut `NavController`-it, tekivad segased navivead (ekraan ei vahetu, route ei klapi, back stack käitub valesti).

---

## 5) Kuidas testida (kiirkontroll)

1. Käivita app.
2. Oodatud: avaneb `Login` ekraan.
3. Sisesta vigane email/parool → nupp disabled või veateated nähtavad.
4. Sisesta korrektne email + vähemalt 6 märki parool → nupp aktiivne.
5. Vajuta “Logi sisse” → avaneb Home ekraan.

---

## 6) Kui Login ei ilmu

Kontrolli:
- kas `startDestination` on kindlasti `LOGIN`
- kas `composable(AppDestinations.LOGIN)` on NavHostis olemas
- kas kuskil pole automaatset `navigate(HOME)` käivituse ajal
- kas route stringid on identsed (`"login"` vs `"Login"`)

---

## 7) Kokkuvõte

Samm on valmis:
- Login route on lisatud
- LoginScreen on eraldi feature kaustas
- NavGraph avab esmalt Login vaate
- Edasi liigub Home’i kontrollitud callbacki kaudu

Järgmine loogiline samm:
- lisada `LoginViewModel` ja fake auth flow (ilma Firebase’ita),
- seejärel asendada fake auth päris backend/Firebase autentimisega.
