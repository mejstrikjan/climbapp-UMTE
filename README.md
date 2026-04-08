# Semester Climb

Nativní Android aplikace v Kotlinu vytvořená jako semestrální projekt. Projekt vychází z původní lezecké aplikace, ale je přepsaný do čistého Kotlin / Jetpack Compose řešení.

## Co projekt obsahuje

- 6 obrazovek:
  - `Cesty`
  - `Deník`
  - `Mapa`
  - `Oblasti`
  - `Profil`
  - formuláře pro přidání a úpravy dat
- `Room` persistenci pro oblasti, skály, sektory, cesty, výstupy, session a fotky
- `DataStore` pro uživatelské preference
- `Hilt` dependency injection
- `Mapy.com REST API` pro mapové dlaždice a práci s polohou oblasti
- foto workflow přímo ve formuláři cesty
- blokující I/O mimo UI thread přes repository vrstvu a coroutines

## Hlavní funkcionalita

- správa oblastí včetně typu `sport / boulder / trad / indoor`
- hierarchie `oblast -> skála -> sektor`
- záznam výstupů a session
- výběr polohy oblasti kliknutím do mapy
- ukládání fotky cesty přímo při vytváření nebo editaci cesty
- lokální databáze bez nutnosti backendu

## Mapování na zadání

- `alespoň 5 obrazovek`: splněno
- `blocking I/O mimo UI thread`: splněno přes repository vrstvu a coroutines
- `datová persistence`: splněno pomocí `Room` a `DataStore`
- `vhodné uspořádání souborů`: splněno, projekt je rozdělený na `data`, `di`, `navigation`, `ui`, `viewmodel`
- `dependency injection`: splněno pomocí `Hilt`
- `něco navíc`: splněno, aplikace obsahuje mapu a foto workflow

## Struktura

- `app/src/main/java/cz/climb/semester/data` - Room, DataStore, repository
- `app/src/main/java/cz/climb/semester/di` - Hilt moduly
- `app/src/main/java/cz/climb/semester/navigation` - Compose navigace
- `app/src/main/java/cz/climb/semester/ui` - Compose obrazovky a komponenty
- `app/src/main/java/cz/climb/semester/viewmodel` - ViewModel vrstva

## Build

```powershell
cd C:\semester-kotlin-app
.\gradlew assembleDebug
```

Výstupní APK po úspěšném buildu:

`C:\semester-kotlin-app\app\build\outputs\apk\debug\app-debug.apk`

## Poznámky k mapě

Build si čte Mapy.com klíč ze souboru `C:\climbApp\.env.local`.
