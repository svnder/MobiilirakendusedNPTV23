# Juhend: Esimese Jetpack Compose rakenduse loomine ja automaattestimine

Selles õppematerjalis ehitame Android Studios interaktiivse rakenduse, mis sisaldab tekstisisestust ja loendurit. Seejärel vaatame, kuidas rakendust manuaalselt testida ning kuidas kirjutada Jetpack Compose'i raamistikus automaatne kasutajaliidese (UI) test, mis kogu stsenaariumi ise läbi mängib.

---

## 1. Projekti seadistamine ja rakenduse kood

Kõigepealt loome rakenduse, millel on selge seisund (*state*) ja mida on hiljem lihtne testida.

1. Ava Android Studio ja loo uus projekt (**New Project**).
2. Vali malliks **Empty Activity** (kasutab vaikimisi Jetpack Compose'i).
3. Ava fail `MainActivity.kt` ja asenda sealne sisu alloleva koodiga.
   *(Märkus: Jälgi, et faili kõige esimene rida vastaks sinu projekti tegelikule paketinimele, nt `package com.sinu.paketinimi`, muidu rakendus ei käivitu!)*

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CounterScreen()
                }
            }
        }
    }
}

@Composable
fun CounterScreen() {
    var count by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tekstisisestusväli
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Sisesta oma nimi") },
            modifier = Modifier.testTag("nameInput")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dünaamiline tervitustekst
        Text(
            text = if (name.isNotBlank()) "Tere, $name!" else "Tere, külaline!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("greetingText")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Väärtus: $count",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("countText")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { count-- }, modifier = Modifier.testTag("decrementButton")) {
                Text("-1")
            }
            Button(onClick = { count++ }, modifier = Modifier.testTag("incrementButton")) {
                Text("+1")
            }
        }
    }
}
```

### Mis on `testTag`?
Koodis on kasutusel element `Modifier.testTag("nimi")`. See on nähtamatu nimesilt või triipkood. Kuna automaattest on "pime" ega näe ekraani visuaalselt, võimaldab see silt testil tuhandete elementide seast täpselt õige tekstivälja või nupu üles leida.

---

## 2. Manuaalne testimine

Enne automaattestide kirjutamist on oluline veenduda, et rakendus põhimõtteliselt töötab.

1. Vajuta Android Studios üleval asuvat rohelist **"Play"** nuppu (Run 'app').
2. Kui rakendus avaneb emulaatoris või telefonis, testi seda visuaalselt ja füüsiliselt:
   * Sisesta tekstiväljale nimi ja jälgi, kas tervitus muutub.
   * Klõpsa "+1" ja "-1" nuppe ning kontrolli, kas matemaatika peab paika.
3. **Vigade lahendamine:** Kui rakendus ei käivitu, ava allservast **Logcat** või **Run** vaade. Otsi punast teksti (näiteks `java.lang.ClassNotFoundException`), mis viitab tavaliselt valele paketinimele või kompileerimisveale.

---

## 3. Automaattestimine (UI test)

Manuaalne testimine on aeganõudev. Automaattest suudab aga terve kasutajateekonna (*user journey*) läbi mängida sekundi murdosaga.

### Testfaili loomine
Kõik kasutajaliidese testid (mis vajavad emulaatorit) asuvad projektis `androidTest` kaustas.
1. Liigu vasakul menüüs: `app` -> `src` -> `androidTest` -> `java` -> `sinu.paketi.nimi`.
2. Loo uus Kotlin klass nimega **`CounterTest`**.
3. Kopeeri faili allolev kood:

```kotlin
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class CounterTest {

    // Reegel, mis käivitab MainActivity enne testi algust
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun complex_user_journey_test() {
        // 1. Kontrollime, et alguses on tervitus anonüümne
        composeTestRule.onNodeWithTag("greetingText").assertTextEquals("Tere, külaline!")

        // 2. Sisestame tekstiväljale nime (imiteerib klaviatuuri kasutamist)
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Android")

        // 3. Kontrollime, kas tervitustekst muutus koheselt
        composeTestRule.onNodeWithTag("greetingText").assertTextEquals("Tere, Android!")

        // 4. Teeme kolm kiiret klikki "+1" nupul
        composeTestRule.onNodeWithTag("incrementButton").performClick()
        composeTestRule.onNodeWithTag("incrementButton").performClick()
        composeTestRule.onNodeWithTag("incrementButton").performClick()

        // 5. Teeme ühe kliki "-1" nupul
        composeTestRule.onNodeWithTag("decrementButton").performClick()

        // 6. Kontrollime, kas lõplik matemaatika klapib (3 - 1 = 2)
        composeTestRule.onNodeWithTag("countText").assertTextEquals("Väärtus: 2")
    }
}
```

### Kuidas automaattesti käivitada?
1. Ava loodud `CounterTest.kt` fail.
2. Vajuta rea `@Test fun complex_user_journey_test()` vasakus servas asuvat rohelist kolmnurka (Play ikoon).
3. Vali **Run**. 
4. Arvuti avab korraks emulaatoris äpi, leiab `testTag`-ide abil nupud üles, sisestab teksti, teeb klikid ja paneb äpi kinni. Kui kõik toimis ootuspäraselt, ilmub Android Studio allserva roheline linnuke (**Test passed**).

> **Hea tava:** Kuigi see näide mängib läbi pika stsenaariumi, on tarkvaraarenduses soovitatav luua ka väiksemaid teste, mis kontrollivad ainult ühte asja korraga (näiteks eraldi test ainult liitmise jaoks). See teeb tulevikus vigade asukoha tuvastamise palju lihtsamaks.
