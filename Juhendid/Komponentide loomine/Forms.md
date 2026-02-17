# Forms põhi (Compose) — enne LoginScreeni

See samm ehitab valmis standardsed formikomponendid, mida saad kasutada loginis, sign up’is, profiili muutmisel ja postituse loomisel.

Fookus:
- **sisend + veateade**
- **parooli show/hide**
- **primaarne nupp enabled/disabled loogikaga**
- **lihtne kliendipoolne valideerimine**

---

## Miks see samm on oluline?

Enne backend/auth ühendamist on mõistlik UI ja valideerimine paika saada:
1. kasutajakogemus muutub kohe paremaks;
2. hiljem lisad ainult API/Firebase kõne;
3. vigade hulk väheneb, sest field-state on juba läbimõeldud.

---

## Kaust ja failid

Soovituslik asukoht:

- package: `com.example.blogiapp.feature_common.ui.components`
- fail: `FormComponents.kt`

See hoiab komponendid ühes kohas ja väldib duplikaate eri ekraanidel.

---

## 1) FormComponents.kt (komponendid)

```kotlin
package com.example.blogiapp.feature_common.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/* KOMMENTAAR
Input with error state.
Kui isError = true, näitab all errorText.
*/
@Composable
fun AppTextInputWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String = ""
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (isError && errorText.isNotBlank()) {
            Text(
                text = errorText,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/* KOMMENTAAR
Password input:
- show/hide parool
*/
@Composable
fun AppPasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = "Toggle password visibility"
                )
            }
        }
    )
}

/* KOMMENTAAR
Primary nupp.
enabled = false korral ei saa vajutada.
*/
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
```

---

## 2) HomeScreenis testimine (ajutine demo)

```kotlin
package com.example.blogiapp.feature_home.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogiapp.feature_common.ui.components.AppPasswordInput
import com.example.blogiapp.feature_common.ui.components.AppPrimaryButton
import com.example.blogiapp.feature_common.ui.components.AppTextInputWithError

@Composable
fun HomeScreen() {
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
            androidx.compose.material3.Text("Parool peab olema vähemalt 6 tähemärki")
        }

        AppPrimaryButton(
            text = "Jätka",
            enabled = formValid,
            onClick = {
                Log.d("FORM_TEST", "Form OK: email=$email")
            }
        )
    }
}
```

---

## Oodatud tulemus

- Email väljal kuvatakse viga, kui sisendis puudub `@`.
- Password väljal saab silmaikooniga teksti peita/näidata.
- Nupp **Jätka** on aktiivne ainult siis, kui:
  - email sisaldab `@`
  - parooli pikkus on vähemalt 6

---

## Tähelepanu punktid (olulised aspektid)

### 1) State peab olema ekraani tasemel
`email`, `password`, `passwordVisible` hoia parent composable’is (nt `HomeScreen` või hiljem `LoginScreen`), mitte komponendi sees.  
Nii saad loogikat kontrollida ühest kohast ja testimine on lihtsam.

### 2) Komponendid olgu “dumb UI”
`AppTextInputWithError`, `AppPasswordInput`, `AppPrimaryButton` ei peaks teadma backendist ega repositoryst.  
Nad võtavad vastu state’i ja callbackid.

### 3) Valideerimine enne võrku
Lihtne local validation (email formaat, min pikkus) vähendab tarbetuid auth/API päringuid ja parandab UX-i.

### 4) Nupu enabled loogika
`enabled = formValid` on kõige selgem viis kasutajale näidata, millal saab edasi minna.

### 5) Logid testi ajal
Kasuta `Log.d("FORM_TEST", "...")` arenduse ajal; release’is ära logi tundlikke andmeid (nt parool).

### 6) Importide korrashoid
Kui tekib punane error:
- `Alt+Enter` (auto-import)
- vajadusel **Sync Project with Gradle Files**
- **Rebuild Project**

---

## Kiire kontrollnimekiri

- [ ] Mul on `feature_common.ui.components` olemas.
- [ ] `FormComponents.kt` fail kompileerub vigadeta.
- [ ] Email validation töötab.
- [ ] Password show/hide töötab.
- [ ] Primary nupp aktiveerub ainult korrektse sisendi korral.
- [ ] `FORM_TEST` logi ilmub Logcatis.

---

## Järgmine samm

Kui see on paigas, järgmine loogiline samm on:
- sama komponendikomplektiga eraldi `LoginScreen`
- seejärel ViewModel + auth use case (backend/Firebase ühendus).
