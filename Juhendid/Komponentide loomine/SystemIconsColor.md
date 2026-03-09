# BlogiApp — System Bars ja Dark Mode runtime juhend
## Ametlik tehniline dokumentatsioon

## Eesmärk

Tagada, et status bari ja navigation bari ikoonivärvid vahetuvad korrektselt koos rakenduse dark mode lülitiga runtime ajal.

Dokumendis kirjeldatud lahendus väldib olukorda, kus tumedal taustal jäävad süsteemiriba ikoonid mustaks.

---

## Probleemi olemus

Kui süsteemiribade stiil määratakse ainult `onCreate` hetkel, siis teema runtime muutused (Profile switch) ei pruugi status bari ja navigation bari ikoone uuendada.  
Tulemuseks on visuaalne vastuolu: tumedal taustal tumedad ikoonid või vastupidi.

---

## Tehniline lähenemine

Süsteemiribade ikooni-appearance tuleb siduda Compose teemaolekuga (`darkTheme`) ja rakendada see `SideEffect` plokis.  
Nii uuendatakse süsteemiribasid iga kord, kui teemaolek muutub.

---

## Muudatusfail

`app/src/main/java/com/example/blogi/MainActivity.kt`

---

## Vajalikud importid

```kotlin
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
```

---

## Rakendatav lahendus

### 1) `onCreate` alguses lihtne edge-to-edge

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
        // ülejäänud Compose sisu
    }
}
```

### 2) `setContent` sees runtime süsteemiriba uuendus

`darkTheme` state lugemise järel lisa:

```kotlin
SideEffect {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)

    insetsController.isAppearanceLightStatusBars = !darkTheme
    insetsController.isAppearanceLightNavigationBars = !darkTheme
}
```

### 3) Teema jääb samaks

```kotlin
BlogiTheme(
    darkTheme = darkTheme,
    dynamicColor = false
) {
    AppEntry(
        darkTheme = darkTheme,
        onDarkThemeChange = { darkTheme = it }
    )
}
```

---

## Miks see lahendus töötab

`SideEffect` käivitub pärast edukat recompositionit.  
Kui `darkTheme` muutub, uuendatakse süsteemiribade ikooni-appearance kohe sama oleku järgi.

`isAppearanceLightStatusBars` ja `isAppearanceLightNavigationBars` loogika:
- `true` tähendab tumedaid ikoone (heledale taustale)
- `false` tähendab heledaid ikoone (tumedale taustale)

Seetõttu kasutatakse väljendit `!darkTheme`.

---

## Levinud vead

### Viga 1
Süsteemiriba stiil määratakse ainult `onCreate` alguses ja eeldatakse, et see muutub koos switchiga.

**Tagajärg:** runtime teema muutus ei kajastu süsteemiriba ikoonides.

---

### Viga 2
`enableEdgeToEdge(statusBarStyle = ..., navigationBarStyle = ...)` plokis kasutatakse vales kohas muutujat või segatakse `auto` ja `dark/light` lähenemisi.

**Tagajärg:** ebastabiilne tulemus eri seadmetel ja emulaatorites.

---

### Viga 3
Koodis on nime-ebakõla (`isDarkNow`, `isDarkMode`, `darkTheme`).

**Tagajärg:** vale tingimus, vale ikoonivärv või kompileerimisviga.

---

## Soovitatud praktika

Hoia süsteemiribade runtime juhtimine ühes kohas (`MainActivity` `setContent` sees).  
Hoia teemaallikas (`darkTheme`) üksikuks tõeallikaks.  
Väldi paralleelset lähenemist, kus osa loogikat on `onCreate` stiiliplokkides ja osa `SideEffect` sees.

---

## Oodatav tulemus

Pärast muudatuse rakendamist:
- Dark mode ON korral on süsteemiriba ikoonid heledad.
- Dark mode OFF korral on süsteemiriba ikoonid tumedad.
- Muutus toimub kohe switchi vajutamisel, ilma rakenduse taaskäivituseta.
