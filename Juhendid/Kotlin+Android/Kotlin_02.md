

## 1) Activity elutsükkel

Activity lifecycle kirjeldab, millal ekraan luuakse, läheb nähtavaks, saab fookuse, läheb taustale ja hävitatakse.  
Kõige olulisemad callbackid: `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`.

- **onCreate**: esmane setup (UI, alginitsialiseerimine)
- **onStart**: Activity on nähtav
- **onResume**: Activity on foregroundis ja kasutaja saab suhelda
- **onPause**: fookus kaob (tee kiire salvestus/peatus)
- **onStop**: Activity pole nähtav (vabasta raskemad ressursid)
- **onDestroy**: lõplik cleanup

```kotlin
package com.example.blogiapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {

    companion object { private const val TAG = "ACTIVITY_LIFECYCLE" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContent { TervitusApp() }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}

@Composable
fun TervitusApp() { Text("Activity lifecycle demo") }
```

**Kuidas katsetada**
1. Käivita app.
2. Ava Logcat ja filtreeri `ACTIVITY_LIFECYCLE`.
3. Vajuta Home, tagasi appi, Back, pööra ekraani — jälgi callbackide järjekorda.

---

## 2) Fragmenti alused

Fragment on taaskasutatav UI-osa Activity sees.  
Praktikas: üks Activity + mitu Fragmenti on klassikaline ülesehitus.

- Fragmentil on oma lifecycle (`onCreateView`, `onViewCreated`, ...)
- UI luuakse tavaliselt `onCreateView`/`onViewCreated`
- Navigation Componentiga on fragmentide vahetamine lihtsam

```kotlin
package com.example.blogiapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    companion object { private const val TAG = "FRAGMENT_DEMO" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }
}
```

**Kuidas katsetada**
1. Loo `fragment_home.xml`.
2. Lisa Activity layouti `FragmentContainerView`.
3. Ava Logcat ja filtreeri `FRAGMENT_DEMO`.

---

## 3) XML paigutused

XML layout määrab klassikalises Androidis UI struktuuri.  
Levinud containerid: `LinearLayout`, `ConstraintLayout`, `FrameLayout`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/titleText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tere XML maailm"
        android:textSize="20sp" />

    <Button
        android:id="@+id/actionButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Vajuta" />
</LinearLayout>
```

**Kuidas katsetada**
1. Loo `res/layout/activity_main.xml`.
2. Ava see Design + Code režiimis.
3. Seosta Activitys: `setContentView(R.layout.activity_main)`.

---

## 4) Intentid ja navigeerimine

Intentiga avad teise Activity või saadad andmeid.

```kotlin
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FirstActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val intent = Intent(this, SecondActivity::class.java)
        intent.putExtra("username", "Mari")
        startActivity(intent)
    }
}

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val username = intent.getStringExtra("username")
        // kasuta username väärtust UI-s
    }
}
```

**Kuidas katsetada**
1. Loo `FirstActivity` ja `SecondActivity`.
2. Lisa mõlemad `AndroidManifest.xml` faili.
3. Käivita `FirstActivity`, kontrolli, et `SecondActivity` avaneb ja saab andme kätte.

---

## 5) ViewBinding kasutus

ViewBinding annab type-safe viited XML viewdele ilma `findViewById`-ta.

**Gradle**
```kotlin
android {
    buildFeatures {
        viewBinding = true
    }
}
```

**Activity näide**
```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blogiapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleText.text = "ViewBinding töötab"
        binding.actionButton.setOnClickListener {
            binding.titleText.text = "Nuppu vajutati"
        }
    }
}
```

**Kuidas katsetada**
1. Lülita `viewBinding = true`.
2. Sync Gradle.
3. Veendu, et `ActivityMainBinding` tekib.
4. Käivita app ja vajuta nuppu.

---

## 6) Ressursid ja stringid

Ressursid hoiavad tekste, värve, mõõte jne eraldi failides.  
See teeb lokaliseerimise ja hoolduse lihtsaks.

**`res/values/strings.xml`**
```xml
<resources>
    <string name="app_name">BlogiApp</string>
    <string name="welcome_text">Tere tulemast!</string>
</resources>
```

**Kasutus XML-is**
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/welcome_text" />
```

**Kasutus Kotlinis**
```kotlin
val welcome = getString(R.string.welcome_text)
```

**Kuidas katsetada**
1. Lisa uus string `strings.xml`.
2. Kasuta seda XML-is ja/või Kotlinis.
3. Muuda stringi väärtust ning käivita uuesti — UI muutub automaatselt.

---

## 7) Logcat ja debug

Logcat on peamine tööriist, et näha mis koodis toimub.

```kotlin
import android.util.Log

private const val TAG = "DEBUG_DEMO"

fun doWork() {
    Log.d(TAG, "Töö algas")
    try {
        val result = 10 / 2
        Log.d(TAG, "Tulemus: $result")
    } catch (e: Exception) {
        Log.e(TAG, "Viga: ${e.message}", e)
    }
}
```

**Debug põhipraktika**
- Pane breakpoint (rea vasakusse serva)
- Käivita **Debug**
- Kontrolli muutujate väärtusi
- Astu ridade vahel (`Step Over`, `Step Into`)

**Kuidas katsetada**
1. Lisa `Log.d` read nupuvajutuse või lifecycle callbacki sisse.
2. Ava Logcat, filtreeri TAG järgi.
3. Lisa breakpoint ja käivita Debug režiimis.

---

