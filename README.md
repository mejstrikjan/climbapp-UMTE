# Semester Climb

Zjednodusena nativni Android aplikace v Kotlinu, vytvorena jako samostatna kopie puvodniho climbing logu pro semestralni projekt.

## Co projekt obsahuje

- 6 obrazovek:
  - `Dashboard`
  - `Oblasti`
  - `Cesty`
  - `Session`
  - `Profil`
  - `Kamera`
- `Room` persistenci pro oblasti, cesty, session, vystupy a fotky z kamery
- `DataStore` pro uzivatelske preference
- `Hilt` dependency injection
- `Mapy.com REST API` map tiles primo na dashboardu
- `CameraX` preview + foceni
- blokujici I/O mimo UI thread pres repository vrstvu a coroutines

## Mapovani na zadani

- `alespon 5 obrazovek`: splneno, aplikace ma 6 obrazovek
- `blocking I/O mimo UI thread`: splneno, Room / DataStore / camera file I/O bezi mimo UI thread pres repository vrstvu a `Dispatchers.IO`
- `datova persistence`: splneno, pouzity `Room` a `DataStore`
- `vhodne usporadani souboru`: splneno, projekt je rozdeleny na `data`, `di`, `navigation`, `ui`, `viewmodel`
- `dependency injection`: splneno, pouzity `Hilt`
- `neco navic`: splneno, integrovana `kamera` a `Mapy.com REST API`

## Struktura

- `app/src/main/java/cz/climb/semester/data` - Room, DataStore, repository
- `app/src/main/java/cz/climb/semester/di` - Hilt moduly
- `app/src/main/java/cz/climb/semester/navigation` - Compose navigace
- `app/src/main/java/cz/climb/semester/ui` - Compose obrazovky a komponenty
- `app/src/main/java/cz/climb/semester/viewmodel` - ViewModel vrstva

## Build

```powershell
cd C:\climbApp\semester-kotlin-app
.\gradlew assembleDebug
```

Vystupni APK po uspesnem buildu:

`C:\climbApp\semester-kotlin-app\app\build\outputs\apk\debug\app-debug.apk`

## Poznamka

Projekt je zamerne jednodussi nez puvodni React Native aplikace. Cilem je splnit zadani semestralni prace cistym nativnim Kotlin Android projektem, ne prenest celou puvodni funkcionalitu 1:1. Mapovy nahled pouziva `Mapy.com REST API` a build si cte klic ze souboru `C:\climbApp\.env.local`.
