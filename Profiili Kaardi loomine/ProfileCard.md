# ProfileCard komponent (Compose) — samm-sammult

Selles etapis lõime taaskasutatava **ProfileCard** komponendi, et profiilivaade oleks visuaalselt selge ja kood püsiks puhas.

Komponent sobib kasutamiseks:
- `ProfileScreen` ekraanil
- kasutaja detailvaates
- admin paneelis (väikeste muudatustega)

---

## Eesmärk

Luua standardne profiilikaart, kus on:
- avatar placeholder
- nimi + email
- bio
- statistika (followers/following)
- tegevusnupp (`Edit Profile`)

---

## 1) Kaust ja fail

Asukoht:
- package: `com.example.blogiapp.feature_common.ui.components`
- fail: `ProfileCard.kt`

---

## 2) ProfileCard komponendi kood

```kotlin
package com.example.blogiapp.feature_common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/* KOMMENTAAR
Lihtne ja taaskasutatav profiilikaart.
- avatar (praegu placeholder ring)
- nimi + email
- bio
- follower / following
- edit profile nupp
*/
@Composable
fun ProfileCard(
    name: String,
    email: String,
    bio: String,
    followers: Int,
    following: Int,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar placeholder
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    shape = CircleShape
                ) {
                    // lihtne placeholder tekst
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(name.take(1).uppercase())
                    }
                }

                Column {
                    Text(text = name)
                    Text(text = email)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = bio)

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Followers: $followers")
                Text("Following: $following")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
        }
    }
}
```

---

## 3) Test `ProfileScreen` sees

Fail: `feature_profile/ui/ProfileScreen.kt`

```kotlin
package com.example.blogiapp.feature_profile.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogiapp.feature_common.ui.components.ProfileCard

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ProfileCard(
            name = "Mari Maasikas",
            email = "mari@example.com",
            bio = "Õpin Android arendust ja jagan blogipostitusi.",
            followers = 128,
            following = 84,
            onEditClick = {
                Log.d("PROFILE_UI", "Edit Profile clicked")
            }
        )
    }
}
```

---

## 4) Olulised aspektid, mida tähele panna

### 1) Komponent on “reusable”
`ProfileCard` ei sisalda kõvaks kirjutatud andmeid, vaid kõik tuleb parameetritena (`name`, `email`, `bio` jne).  
See teeb komponendi korduvkasutatavaks eri ekraanidel.

### 2) Callback hoiab UI puhtana
`onEditClick` callback tähendab, et kaart ise ei tea navigeerimisest ega backendist.  
Ta ainult teavitab parentit, et nuppu vajutati.

### 3) Layout on loetav
`Column + Row + Spacer` annab algajale selge struktuuri:
- ülemine rida: avatar + nimi/email
- keskel: bio
- all: statistika + nupp

### 4) Placeholder avatar
Praegu kasutame lihtsat ringi ja esimese tähe initsiaali.  
Hiljem saab selle asendada päris pildiga (nt Coil `AsyncImage`).

---

## 5) Oodatud tulemus

Profile ekraanil kuvatakse:
- kaardikomponent
- ringikujuline avatar placeholder
- nimi ja email
- bio tekst
- followers/following väärtused
- Edit Profile nupp

Nupuvajutusel:
- Logcatis tagiga `PROFILE_UI` ilmub logi.

---

## 6) Tüüpilised vead ja parandused

1. **`Divider` on deprecated hoiatus**
   - Võid kasutada `HorizontalDivider()` (Material3 uuem API), kui soovid warningut vältida.

2. **Importide probleem**
   - kasuta `Alt+Enter` auto-importi
   - vajadusel `Sync Project with Gradle Files`

3. **Name tühi väärtus**
   - `name.take(1)` tühja stringi korral annab tühja tulemuse.  
   - Võid lisada fallbacki:
   ```kotlin
   val initial = name.firstOrNull()?.uppercase() ?: "?"
   ```

---

## 7) Kokkuvõte

Samm on tehtud:
- `ProfileCard` komponent loodud
- `ProfileScreen` test tehtud
- struktuur sobib hästi modulaarse arhitektuuriga

Järgmine soovituslik samm:
- lisada avatar pilt URL-ist
- või lisada kaardile `Settings` / `Logout` actionid.
