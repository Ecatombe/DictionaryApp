# DictionaryApp

Android dictionary app. Single module, single screen. Searches a public API, caches results in Room, renders with Jetpack Compose. 100% Kotlin.

## Run

```bash
./gradlew assembleDebug       # build
./gradlew installDebug        # install on device/emulator (API 26+)
```

Or press Run in Android Studio.

## Test

```bash
./gradlew test                  # JVM unit tests
./gradlew connectedAndroidTest  # instrumented (requires device/emulator)
```

No real business-logic tests exist yet. The declared stack is JUnit 4, Espresso, and `compose-ui-test-junit4`. Test method names use `snake_case_describing_what_is_verified`.

## Architecture

Clean Architecture, feature-sliced. One feature: `feature_dictionary/{data,domain,presentation,di}`.

- **domain** — pure Kotlin. Models, repository interface, use cases. No Android imports.
- **data** — `*RepositoryImpl`, Room (`*Entity`, `*Dao`, `*Database`), Retrofit DTOs (`*Dto`), type converters.
- **presentation** — `*ViewModel` (`@HiltViewModel`), `*State` data class, composables.
- **di** — one `@Module @InstallIn(SingletonComponent::class) object` per feature. All bindings `@Singleton`.

Cross-feature utilities live in `core/`.

## Key patterns

**Offline-first / single source of truth.** Repository emits cached Room data immediately, fetches network, writes to DB, re-reads from DB as the success emission. Never emit raw API responses to the domain layer.

**`Resource<T>` wrapper.** All repository flows return `Flow<Resource<T>>`. Errors carry stale data alongside the message. The ViewModel exhausts the `when` branches and routes errors to `_eventFlow` (snackbar), never to persistent state.

**Use cases as callable objects.** `operator fun invoke` — call site reads `getWordInfo(query)`, not `getWordInfo.invoke(query)`.

**Debounce via cancellable Job.** `searchJob?.cancel()` then `delay(500L)` — no dedicated debounce operator.

**State via `data class` + `.copy()`.** One `*State` data class per screen, all mutations through `.copy()`.

**Private/public state pairs.** `_foo` (mutable, private) + `foo` (read-only, public). One-time events via `MutableSharedFlow` exposed as `asSharedFlow()`.

## Naming

| Thing | Convention | Example |
|---|---|---|
| Feature packages | `feature_` prefix | `feature_dictionary` |
| DTOs | `*Dto` | `WordInfoDto` |
| Room entities | `*Entity` | `WordInfoEntity` |
| Repository impls | `*Impl` | `WordInfoRepositoryImpl` |
| Use cases | verb-noun, no suffix | `GetWordInfo` |
| ViewModel event handlers | `on*` | `onSearch` |
| Private mutable state | `_` prefix | `_state`, `_eventFlow` |

## Comments

Inline only, lowercase, explain intent not mechanics. No KDoc/Javadoc. No logging — errors surface via `Resource.Error` + snackbar.

## House style

**Naming.** Feature packages prefixed `feature_`. Class suffixes are mandatory by role: `*Dto`, `*Entity`, `*Impl`, `*Database`. Use cases are verb-noun with no suffix (`GetWordInfo`). ViewModel event handlers prefixed `on*`. Private mutable state prefixed `_`, paired with a public read-only exposure of the same name.

**State.** Screen state is a single `data class` with defaults; all mutations via `.copy()`. One-time UI events (snackbars) go through a `MutableSharedFlow<UIEvent>` exposed as `asSharedFlow()`, never written into persistent state.

**Error handling.** Catch only `HttpException` and `IOException` at the repository boundary. Wrap both in `Resource.Error` with a user-facing message and carry stale cached data alongside. Never let exceptions propagate to the ViewModel. Never catch silently.

**Comments.** Inline only, lowercase, one line, explain intent not mechanics. No KDoc or Javadoc blocks. No `Log.*` calls — errors surface via `Resource.Error` + snackbar.

**Compose.** All composables take `modifier: Modifier = Modifier` as their second parameter. Hard-coded `sp`/`dp` values are acceptable at this scale — no design token abstraction.

**DI.** One `object WordInfoModule` per feature, installed in `SingletonComponent`. All bindings `@Singleton`. Bind via `@Provides` returning the impl directly — not `@Binds`.

**Tests.** Method names in `snake_case_describing_what_is_verified`. Target real behaviour through fakes of the repository interface — no mocking of Room or Retrofit directly.

## Never do

- Don't put business logic in the ViewModel or composables — it belongs in use cases.
- Don't expose `*Entity` or `*Dto` types past the data layer — map to domain models in the repository.
- Don't emit raw API responses; always write to Room and re-read.
- Don't write to `_state` or `_eventFlow` from outside the ViewModel.
- Don't use XML layouts — UI is Compose-only.
- Don't catch exceptions silently — wrap in `Resource.Error` with a user-facing message.
- Don't add `@Provides` bindings outside the feature's `di/` module.
