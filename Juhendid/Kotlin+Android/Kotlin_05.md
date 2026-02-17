
## 1) Clean Architecture alused

Clean Architecture eesmärk on eraldada kood kihtidesse, et projekt oleks testitav ja hooldatav.

Tüüpiline jaotus:
- **Presentation**: UI + ViewModel
- **Domain**: use case’id + ärireeglid
- **Data**: API, DB, repository implementatsioon

Põhireegel:
- väliskiht võib sõltuda sissepoole,
- sisekiht ei tohi sõltuda väljapoole (nt domain ei tohiks teada Retrofitist).

```kotlin
/* KOMMENTAAR
Presentation -> Domain -> Data
UI kutsub use case'i, use case kutsub repositoryt.
*/
```

**Kuidas katsetada**
1. Loo paketid: `presentation`, `domain`, `data`.
2. Tee üks use case `GetPostsUseCase`.
3. Pane UI kutsuma ainult use case’i, mitte API-t otse.

---

## 2) Dependency Injection (Hilt)

Hilt aitab sõltuvusi (dependency) automaatselt “süstida” klassidesse.  
Nii ei loo klassid ise oma sõltuvusi (`Retrofit()`, `Repository()`), vaid saavad need väljast.

```kotlin
// build.gradle (app)
// implementation("com.google.dagger:hilt-android:2.x")
// kapt("com.google.dagger:hilt-compiler:2.x")

// Application
@HiltAndroidApp
class BlogiApp : Application()

// Module
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRepository(): PostRepository = PostRepositoryImpl()
}

// ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel()
```

**Kuidas katsetada**
1. Lisa Hilt dependencyd.
2. Märgi `Application` klass `@HiltAndroidApp`.
3. Tee lihtne `@Provides` repository jaoks.
4. Kontrolli, et ViewModel saab repository ilma käsitsi konstrueerimata.

---

## 3) Repository muster

Repository peidab andmete päritolu (API/DB/cache) UI eest.  
ViewModel räägib repository interface’iga, mitte konkreetse Retrofit/Room klassiga.

```kotlin
data class Post(val id: Int, val title: String)

interface PostRepository {
    suspend fun getPosts(): List<Post>
}

class PostRepositoryImpl(
    private val api: BlogApi,
    private val dao: PostDao
) : PostRepository {
    override suspend fun getPosts(): List<Post> {
        val remote = api.getPosts()
        // map + cache
        dao.insertAll(remote.map { PostEntity(it.id, it.title) })
        return remote.map { Post(it.id, it.title) }
    }
}
```

**Kuidas katsetada**
1. Loo `PostRepository` interface.
2. Loo `PostRepositoryImpl`.
3. Kasuta ViewModelis ainult interface’i.
4. Vaheta implementatsioon mock/fake vastu testis.

---

## 4) Offline-first loogika

Offline-first tähendab, et app töötab ka ilma internetita.  
Tavaline muster:
1. loe andmed lokaalsest DB-st,
2. proovi taustal võrku,
3. õnnestumisel uuenda DB,
4. UI jälgib DB voogu.

```kotlin
class OfflineFirstRepository(
    private val api: BlogApi,
    private val dao: PostDao
) {
    fun observePosts() = dao.observePosts() // Flow<List<PostEntity>>

    suspend fun refreshPosts() {
        try {
            val remote = api.getPosts()
            dao.replaceAll(remote.map { PostEntity(it.id, it.title) })
        } catch (e: Exception) {
            // võrguviga: jäta lokaalsed andmed alles
        }
    }
}
```

**Kuidas katsetada**
1. Käivita app internetiga, lae andmed DB-sse.
2. Lülita internet välja.
3. Ava app uuesti ja kontrolli, et vanad andmed kuvatakse ikka.
4. Lülita internet sisse ja kontrolli, et DB värskendub.

---

## 5) Unit testid (JUnit)

Unit test kontrollib väikest loogika osa ilma UI-ta.  
Fookus: use case’id, mapperid, ViewModel loogika.

```kotlin
import org.junit.Assert.assertEquals
import org.junit.Test

class SumUseCase {
    fun execute(a: Int, b: Int) = a + b
}

class SumUseCaseTest {

    @Test
    fun `execute returns correct sum`() {
        val useCase = SumUseCase()
        val result = useCase.execute(2, 3)
        assertEquals(5, result)
    }
}
```

**Kuidas katsetada**
1. Loo testklass `src/test/...`.
2. Lisa `@Test` meetod.
3. Run test (roheline ikoon).
4. Muuda oodatav väärtus valeks ja vaata, et test kukub läbi.

---

## 6) UI testid (Espresso)

UI test käivitab päris ekraani ja kontrollib nähtavaid elemente/klikke.

```kotlin
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun buttonClick_changesText() {
        onView(withId(R.id.actionButton)).perform(click())
        onView(withId(R.id.titleText)).check(matches(withText("Nuppu vajutati")))
    }
}
```

**Kuidas katsetada**
1. Lisa test `src/androidTest/...`.
2. Veendu, et layoutis on `actionButton` ja `titleText`.
3. Käivita test emulatoris.
4. Kontrolli, et test läheb roheliseks.

---

## 7) Jõudluse optimeerimine

Jõudlus tähendab sujuvat UI-d, väikest mälukulu ja kiiret reageerimist.

Olulised praktikad:
- ära tee võrgu/DB tööd main threadil
- kasuta pagingut suurte listide jaoks
- väldi liigset recompositionit Compose’is
- kasuta profilerit (CPU, Memory, Network)
- cache’i andmeid mõistlikult

```kotlin
// Vale: raske töö Main threadil
// Õige: tee IO Dispatchers.IO peal

viewModelScope.launch(Dispatchers.IO) {
    val posts = api.getPosts()
    dao.insertAll(posts.map { PostEntity(it.id, it.title) })
}
```

**Kuidas katsetada**
1. Ava Android Studio Profiler.
2. Käivita API + listi laadimine.
3. Jälgi CPU ja Memory hüppeid.
4. Lisa optimeerimine (nt paging/caching) ja võrdle näite.

---

