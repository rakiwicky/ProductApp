# Quickstart: Product Search Validation

## Prerequisites

- Android Studio or the project Gradle environment installed
- Xcode 16+ / iOS simulator installed for the app target
- The project is already set up at `/Users/rakiwicky/working/kmp/ProductApp`

## Shared validation

Run the shared checks that cover the common KMP feature logic:

```bash
cd /Users/rakiwicky/working/kmp/ProductApp
./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test --console=plain
```

Expected outcome:
- the shared product test suite passes
- the ViewModel search behavior passes for title/category/price filtering and clear behavior

## Android build validation

```bash
cd /Users/rakiwicky/working/kmp/ProductApp
./gradlew :androidApp:assembleDebug --console=plain
```

Expected outcome:
- the Android app assembles successfully
- the screen still loads and renders with the search field

## iOS validation

```bash
cd /Users/rakiwicky/working/kmp/ProductApp
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17' build
```

Expected outcome:
- the iOS app builds successfully
- the shared search UI renders and behaves consistently with Android

## Manual validation scenarios

1. Open the product list and type a product title; only matching items stay visible.
2. Type a category name; matching category items are shown.
3. Type a price value; the matching price items are displayed.
4. Tap the clear icon; the query clears and the full list returns.
5. Search for a term with no results; the empty-state message is shown.
6. Clear the no-match query; the product list returns without reloading.

