## 1) MVVM põhimudel

MVVM (Model-View-ViewModel) aitab hoida koodi puhta ja testitavana.

- **View**: UI kiht (Activity/Fragment/Compose), kuvab andmeid ja saadab kasutaja tegevused edasi.
- **ViewModel**: hoiab UI state’i ja äriloogikat, ei tea otseselt View detailidest.
- **Model**: andmekiht (API, Room, repository, DTO-d).

Miks see kasulik on:
- vähem segamini loogikat UI sees
- lihtsam testida ViewModelit
- parem skaleeritavus suuremas projektis

```kotlin
/* KOMMENTAAR
MVVM lihtne mõte:
View -> kutsub ViewModelit
ViewModel -> küsib andmed repositoryst
Repository -> toob API/DB andmed
*/
```

**Kuidas katsetada**
1. Loo paketid: `ui`, `viewmodel`, `data`.
2. Pane ekraanile nupp “Lae andmed”.
3. Nupp kutsub ViewModeli funktsiooni.
4. ViewModel uuendab state’i, UI kuvab tulemuse.

---

## 2) ViewModel kasutus

ViewModel elab üle konfiguratsioonimuudatuste (nt ekraani pööramine) ja hoiab UI state’i.

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _title = MutableStateFlow("Algseis")
    val title: StateFlow<String> = _title

    fun loadTitle() {
        viewModelScope.launch {
            _title.value = "Laen..."
            delay(1000)
            _title.value = "Andmed laetud"
        }
    }
}
```

**Kuidas katsetada**
1. Seo ViewModel ekraaniga.
2. Näita `title` väärtus UI-s.
3. Vajuta nuppu, mis käivitab `loadTitle()`.
4. Kontrolli, et tekst muutub: “Laen...” → “Andmed laetud”.

---

## 3) LiveData / StateFlow

Mõlemad on vaatlejapõhised andmehoidjad UI jaoks.

- **LiveData**: klassikaline Android lifecycle-aware observable.
- **StateFlow**: Kotlin Flow põhine, väga levinud modernses MVVM-is.

### LiveData näide
```kotlin
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    private val _name = MutableLiveData("Mari")
    val name: LiveData<String> = _name

    fun updateName(newName: String) {
        _name.value = newName
    }
}
```

### StateFlow näide
```kotlin
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val _theme = MutableStateFlow("Light")
    val theme: StateFlow<String> = _theme

    fun setDarkMode() { _theme.value = "Dark" }
}
```

**Kuidas katsetada**
1. Tee üks nupp, mis muudab väärtust.
2. Seo väärtus UI tekstiga.
3. Vajuta nuppu ja kontrolli, et UI uueneb kohe.

---

## 4) Coroutines alused

Coroutines võimaldavad asünkroonset tööd ilma UI threadi blokeerimata.

Põhielemendid:
- `suspend` funktsioon
- `launch` (tulemust ei tagasta)
- `async/await` (tagastab tulemuse)
- dispatcherid (`Dispatchers.IO`, `Dispatchers.Main`)

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

class CoroutineDemoViewModel : ViewModel() {

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("COROUTINE_DEMO", "Alustan võrgutööd")
            delay(1000)
            Log.d("COROUTINE_DEMO", "Võrgutöö valmis")
        }
    }
}
```

**Kuidas katsetada**
1. Käivita `fetchData()` nupuvajutusel.
2. Ava Logcat filter `COROUTINE_DEMO`.
3. Kontrolli, et UI ei hanguks ja logid ilmuksid järjest.

---

## 5) Retrofit API päringud

Retrofit lihtsustab HTTP päringuid API-dele.

### API interface
```kotlin
import retrofit2.http.GET

data class PostDto(val id: Int, val title: String)

interface BlogApi {
    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
```

### Retrofit builder
```kotlin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    val api: BlogApi = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BlogApi::class.java)
}
```

### ViewModel kasutus
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log

class PostsViewModel : ViewModel() {
    fun loadPosts() {
        viewModelScope.launch {
            try {
                val posts = RetrofitProvider.api.getPosts()
                Log.d("API_DEMO", "Poste: ${posts.size}")
            } catch (e: Exception) {
                Log.e("API_DEMO", "API viga: ${e.message}", e)
            }
        }
    }
}
```

**Kuidas katsetada**
1. Lisa Internet permission manifesti.
2. Käivita `loadPosts()` nupust.
3. Vaata Logcat `API_DEMO`, kas postide arv tuleb.

---

## 6) Room andmebaas

Room on SQLite ümber ehitatud ORM-laadne lahendus Androidile.

Põhiosad:
- `@Entity` (tabel)
- `@Dao` (päringud)
- `@Database` (DB kirjeldus)

```kotlin
import androidx.room.*

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    @Insert
    suspend fun insert(note: NoteEntity)
}

@Database(entities = [NoteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
```

**Kuidas katsetada**
1. Loo DB instance.
2. Sisesta testmärge.
3. Loe kõik märked ja logi nende arv.
4. Kontrolli, et restartides andmed jäävad alles.

---

## 7) Veahaldus mustrid

Hea muster: esita UI-le **seisund** (`Loading`, `Success`, `Error`), mitte toorandmeid ilma kontekstita.

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### ViewModel näide
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ErrorHandlingViewModel : ViewModel() {

    private val _state = MutableStateFlow<UiState<String>>(UiState.Loading)
    val state: StateFlow<UiState<String>> = _state

    fun loadData() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                // Simuleeri edukat vastust
                val result = "Andmed OK"
                _state.value = UiState.Success(result)
            } catch (e: Exception) {
                _state.value = UiState.Error("Midagi läks valesti")
            }
        }
    }
}
```

**Kuidas katsetada**
1. Seo UI `state` vaatlemisega.
2. Näita Loading spinnerit `Loading` puhul.
3. Näita teksti `Success` puhul.
4. Näita veateadet `Error` puhul.

---

