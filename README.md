```md
# ProductApp

ProductApp is an experimental Kotlin Multiplatform (KMP) project built to explore shared app development for Android and iOS.

The app loads product data from a remote API, displays it in a searchable catalog, and allows users to view product details. The project focuses on reusing business logic, UI patterns, and navigation across platforms while keeping native app entry points for Android and iOS.

## Tech Stack
- Kotlin Multiplatform
- Compose Multiplatform
- Material 3
- Koin
- Ktor
- Kotlin Serialization
- Coil

## Features
- Product listing
- Search by title, category, or price
- Pull-to-refresh
- Loading, empty, and error states
- Product detail screen
- Shared navigation and state handling

## Project Structure
```text
ProductApp/
├── androidApp/     # Android app
├── iosApp/         # iOS app
├── shared/         # Shared KMP code
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── gradlew
```

## Run
### Android
```bash
./gradlew :androidApp:assembleDebug
```

### iOS
Open the `iosApp` project in Xcode and run it there.

## Purpose
This project was created as a learning and experimentation exercise in Kotlin Multiplatform development, with a focus on code sharing and cross-platform app structure.

```
