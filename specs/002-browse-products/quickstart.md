# Quickstart: Browse Products

## Prerequisites

- Android SDK and configured Gradle/JDK environment required by the repository.
- Xcode with an iOS simulator runtime for iOS simulator tests and framework linking.
- Network access to `https://fakestoreapi.com/products` and its product image URLs for manual end-to-end checks.

## Automated Validation

From the repository root, run:

```sh
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
./gradlew :androidApp:assembleDebug
```

Link the iOS framework with `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`. Build and run the iOS app from `iosApp/iosApp.xcodeproj` (scheme `iosApp`) in Xcode, or from the command line:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

Feature tests should cover:

1. DTO-to-domain mapping, including `image` to `imageUrl` and unknown response fields.
2. Repository success, HTTP/decoding failure, and cancellation propagation using Ktor MockEngine.
3. Use-case delegation and success/failure results using `FakeProductRepository`.
4. ViewModel initial loading, product success, empty success, initial error/retry, refresh success, and failed refresh preserving displayed products, asserted with Turbine.

## Manual End-to-End Scenarios

1. Start the Android or iOS app with network access. The Products screen begins in a loading state and then displays product images, two-line titles, categories, and consistently formatted USD prices.
2. Scroll the list and pull down. A refresh indicator appears; when the request completes, the list reflects the latest response.
3. Disable network access before opening the screen. Verify a clear initial error and retry action; restore network access and retry to reach the product list.
4. While products are shown, make refresh fail. Verify the list remains visible and the refresh failure is communicated with a retry path.
5. Verify an empty successful repository response displays an empty-state message and an unavailable image displays a neutral placeholder without hiding product text.
