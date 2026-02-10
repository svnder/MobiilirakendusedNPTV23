# Input ja Search Bar (Compose) — tehtud samm

## Eesmärk

Luua standardsed UI komponendid enne backend/auth osa:
- `AppTextInput`
- `AppSearchBar`

Need komponendid asuvad ühes kohas ja on hiljem taaskasutatavad loginis, postituse loomisel, profiilis jne.

---

## 1) Package loomine

Loodud package:

`com.example.blogiapp.feature_common.ui.components`

Android Studio tee:
1. `app > kotlin+java > com.example.blogiapp`
2. **New > Package**
3. nimi: `feature_common.ui.components`

---

## 2) Faili loomine

Loodud fail:

`CommonInputs.kt`

Asukoht:
`feature_common/ui/components/CommonInputs.kt`

---

## 3) Komponentide kood

```kotlin
package com.example.blogiapp.feature_common.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/* KOMMENTAAR
Lihtne üldine text input.
*/
@Composable
fun AppTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

/* KOMMENTAAR
Lihtne search bar:
- vasakul otsingu ikoon
- paremal clear nupp, kui tekst pole tühi
*/
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    label: String = "Search"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    )
}
```

---

## 4) Test HomeScreenis

`HomeScreen.kt` testkood:

```kotlin
package com.example.blogiapp.feature_home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogiapp.feature_common.ui.components.AppSearchBar
import com.example.blogiapp.feature_common.ui.components.AppTextInput

@Composable
fun HomeScreen() {
    var text by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppTextInput(
            value = text,
            onValueChange = { text = it },
            label = "Test input"
        )

        AppSearchBar(
            query = query,
            onQueryChange = { query = it },
            label = "Search posts"
        )
    }
}
```

---

## 5) Oodatud tulemus

- Input väljas saab kirjutada.
- Search väljas saab kirjutada.
- Kui search pole tühi, paremal kuvatakse `X` nupp.
- `X` vajutamisel search tühjeneb.

---

## 6) Kokkuvõte

Inputi teema on edukalt tehtud:
- komponent loodud
- package struktuur korras
- test HomeScreenis toimib

Järgmine soovituslik samm:
- `AppTextInput` error state
- `PasswordInput` (show/hide)
- `PrimaryButton` / `LoadingButton`
