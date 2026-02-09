Kotlini süntaks

Kotlini süntaks on tehtud lühikeseks ja loetavaks. Enamasti ei pea kasutama semikooloneid (`;`), ning koodiplokid on selgelt loogeliste sulgudega. See aitab algajal keskenduda loogikale, mitte liigsele vormistusele. Androidi arenduses on see eriti kasulik, sest UI-kood võib muidu kiiresti pikaks minna.

Kotlinis on põhilised ehitusklotsid funktsioonid (`fun`), muutujad (`val/var`), tingimuslaused (`if/when`) ja klassid. Kui need süntaktiliselt selged on, muutub edasine õppimine palju lihtsamaks.

```kotlin
/* KOMMENTAAR
Põhisüntaks:
- fun = funktsioon
- Log.d = väljund
- if/else = tingimus
*/
import android.util.Log

fun tervita(nimi: String) {
    Log.d("KOTLIN_BASICS", "Tere, $nimi")
}

fun main() {
    val kasutaja = "Mari"
    tervita(kasutaja)

    if (kasutaja.length > 3) {
        Log.d("KOTLIN_BASICS", "Nimi on pikem kui 3 tähemärki")
    } else {
        Log.d("KOTLIN_BASICS", "Nimi on lühike")
    }
}
```

---

Muutujad ja tüübid

Kotlinis kasutatakse peamiselt kahte märksõna: `val` ja `var`. `val` tähendab, et viide on muutumatu (soovituslik vaikimisi valik), `var` tähendab muudetavat väärtust. See distsipliin vähendab vigu: kui asi ei pea muutuma, ära luba seda muuta.

Tüübid võivad olla automaatselt tuletatud (`val arv = 10`) või selgelt määratud (`val arv: Int = 10`). Oluline on ka nulliturvalisus: `String` ei tohi olla `null`, `String?` võib olla `null`.

```kotlin
/* KOMMENTAAR
- val = ei muutu
- var = võib muutuda
- String? = võib olla null
*/
import android.util.Log

fun muutujadNaide() {
    val kool = "BlogiApp Kool"
    var opilasi = 20
    opilasi = 21

    val nimi: String = "Karl"
    val varuNimi: String? = null

    Log.d("KOTLIN_BASICS", "Kool: $kool")
    Log.d("KOTLIN_BASICS", "Õpilasi: $opilasi")
    Log.d("KOTLIN_BASICS", "Nimi: $nimi")
    Log.d("KOTLIN_BASICS", "Varunimi: $varuNimi")
}
```

---

If/when loogika

`if/else` kasutatakse lihtsate tingimuste jaoks. Kotlinis saab `if` olla ka avaldis (tagastab väärtuse), mitte ainult käsulausete blokk. See teeb koodi kompaktseks ja loetavaks.

`when` on mugav mitme haru jaoks (nagu “parem switch”). Seda kasutatakse väga palju menüüde, route’ide, olekute ja kasutajarollide käsitlemisel.

```kotlin
/* KOMMENTAAR
if = kahe haru loogika
when = mitme haru loogika
*/
import android.util.Log

fun loogikaNaide(hinne: Int) {
    val tulemus = if (hinne >= 50) "Läbitud" else "Mitte läbitud"
    Log.d("KOTLIN_BASICS", "If tulemus: $tulemus")

    val kommentaar = when (hinne) {
        in 90..100 -> "Suurepärane"
        in 75..89 -> "Väga hea"
        in 50..74 -> "Rahuldav"
        else -> "Vaja harjutada"
    }
    Log.d("KOTLIN_BASICS", "When kommentaar: $kommentaar")
}
```

---

Tsüklid ja loendid

Tsüklid aitavad sama tegevust korrata, loendid (`List`) hoiavad mitut väärtust koos. Androidis kasutatakse neid pidevalt: näiteks postituste nimekirjad, õpilaste nimekirjad, seaded jne.

Kõige tavalisemad variandid on `for` (iteratsioon) ja `while` (kuni tingimus kehtib). Alguses on mõistlik eelistada lihtsat `for + listOf(...)` kombinatsiooni.

```kotlin
/* KOMMENTAAR
List + for + while põhinäide
*/
import android.util.Log

fun tsuklidJaLoendidNaide() {
    val opilased = listOf("Mari", "Karl", "Anna")

    for (nimi in opilased) {
        Log.d("KOTLIN_BASICS", "Õpilane: $nimi")
    }

    var i = 1
    while (i <= 3) {
        Log.d("KOTLIN_BASICS", "While samm: $i")
        i++
    }
}
```

---

Funktsioonide alused

Funktsioon (`fun`) on korduvkasutatav loogikaplokk. Hea algaja reegel: üks funktsioon teeb ühe asja. Nii on testimine ja veaotsing lihtsam. Funktsioon võib midagi teha (ilma tagastuseta) või tagastada väärtuse.

Parameetrid võimaldavad sama loogikat kasutada erinevate sisenditega. Kotlinis on funktsioonide süntaks lühike ja loetav, mis sobib hästi õpetamiseks.

```kotlin
/* KOMMENTAAR
Funktsioon ilma tagastuseta + funktsioon tagastusega
*/
import android.util.Log

fun tervitaOpilast(nimi: String) {
    Log.d("KOTLIN_BASICS", "Tere tulemast, $nimi!")
}

fun liida(a: Int, b: Int): Int {
    return a + b
}

fun funktsioonideNaide() {
    tervitaOpilast("Mari")
    val summa = liida(5, 7)
    Log.d("KOTLIN_BASICS", "5 + 7 = $summa")
}
```

---

Android Studio UI

Android Studio UI mõttes (Compose puhul) on peamine mõte: ekraan koosneb `@Composable` funktsioonidest. Need on UI-ehitusklotsid (`Text`, `Button`, `Column` jne). Suurem ekraan pannakse kokku väiksematest composable’idest.

Algaja jaoks on oluline aru saada, et `@Composable` märgib funktsiooni, mis joonistab vaadet. UI muutub siis, kui state muutub. Esimeses etapis piisab lihtsast staatilisest ekraanist ja ühest nupust.

```kotlin
/* KOMMENTAAR
@Composable = UI funktsioon
Column paigutab elemendid vertikaalselt
*/
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EsimeneEkraan() {
    Column {
        Text("Minu esimene Compose ekraan")
        Button(onClick = { Log.d("KOTLIN_BASICS", "Nuppu vajutati") }) {
            Text("Vajuta")
        }
    }
}
```

---

Projekti käivitamine

Projekti käivitamine Android Studios tähendab, et valid seadme (emulaator või telefon) ja vajutad Run. Kui kõik on korras, builditakse projekt ja rakendus installitakse seadmesse. See on põhiline “arenda → käivita → kontrolli” tsükkel.

Kui tekib viga, alusta põhilisest kontrollist: importid, package nimed, Gradle Sync, Rebuild. Algaja jaoks on normaalne, et suur osa õppest käibki läbi väikeste vigade parandamise.

```kotlin
/* KOMMENTAAR
Praktiline "kas app töötab?" test:
kirjuta lihtne Composable ja sea see setContent sees.
*/
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TervitusApp()
        }
        Log.d("KOTLIN_BASICS", "MainActivity käivitus")
    }
}

@Composable
fun TervitusApp() {
    Text("App käivitus edukalt")
}
```
