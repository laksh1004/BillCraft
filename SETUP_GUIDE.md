# BillCraft – Developer Setup Guide

> **Audience:** Android developers setting up BillCraft for the first time.
> **Last updated:** 2026-06-20

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Clone and Open the Project](#2-clone-and-open-the-project)
3. [Firebase Setup](#3-firebase-setup)
4. [AdMob Setup](#4-admob-setup)
5. [Build and Run](#5-build-and-run)
6. [Replace Test Ad IDs Before Release](#6-replace-test-ad-ids-before-release)
7. [Keystore Setup for Release Signing](#7-keystore-setup-for-release-signing)
8. [ProGuard / R8 Notes](#8-proguard--r8-notes)
9. [Play Console Setup Checklist](#9-play-console-setup-checklist)
10. [Testing Checklist](#10-testing-checklist)

---

## 1. Prerequisites

Ensure the following tools are installed before proceeding:

| Tool | Minimum Version | Download |
|------|----------------|----------|
| **Android Studio** | Hedgehog (2023.1.1) or later | [developer.android.com/studio](https://developer.android.com/studio) |
| **JDK** | 17 (bundled with Android Studio) | Comes with Android Studio; set Project SDK to Java 17 |
| **Android SDK** | API 33 (minSdk 24, compileSdk 34) | Install via Android Studio SDK Manager |
| **Kotlin** | 1.9.x | Bundled with Android Studio |
| **Git** | Any recent version | [git-scm.com](https://git-scm.com) |

### SDK Manager checklist (Android Studio → Tools → SDK Manager)

- [x] Android 14 (API 34) SDK Platform
- [x] Android SDK Build-Tools 34
- [x] Google Play Services
- [x] Google Repository

---

## 2. Clone and Open the Project

```bash
# Clone the repository
git clone https://github.com/your-org/BillCraft.git

# Open in Android Studio:
# File → Open → select the BillCraft/ folder
```

> **Tip:** Do **not** open the `app/` subfolder — always open the root `BillCraft/` directory so Gradle syncs the whole project.

After opening, Android Studio will trigger a Gradle sync automatically. Wait for it to complete (bottom status bar shows "Gradle sync finished").

---

## 3. Firebase Setup

BillCraft uses **Firebase Analytics** (and optionally Firebase Crashlytics).

### Step-by-step

1. **Create a Firebase project**
   - Go to [console.firebase.google.com](https://console.firebase.google.com)
   - Click **Add project** → name it `BillCraft` → disable Google Analytics for now if you want a minimal setup, or enable it for full funnel tracking.

2. **Add your Android app**
   - In the Firebase console, click **Add app → Android**.
   - **Package name:** `com.billcraft.app` *(must match exactly)*
   - **App nickname:** `BillCraft Android`
   - **Debug signing certificate SHA-1:** Run the command below and paste the output:
     ```bash
     ./gradlew signingReport
     ```

3. **Download `google-services.json`**
   - Firebase will prompt you to download this file.
   - Place it at: `BillCraft/app/google-services.json`
   - ⚠️ **Never commit this file to a public repository.** Add it to `.gitignore`.

4. **Add Firebase SDK (already in `build.gradle` — verify)**
   ```kotlin
   // Root build.gradle.kts
   plugins {
       id("com.google.gms.google-services") version "4.4.0" apply false
       id("com.google.firebase.crashlytics") version "2.9.9" apply false
   }

   // app/build.gradle.kts
   plugins {
       id("com.google.gms.google-services")
   }

   dependencies {
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
       implementation("com.google.firebase:firebase-analytics-ktx")
       implementation("com.google.firebase:firebase-crashlytics-ktx")
   }
   ```

5. **Initialise Analytics in `BillCraftApplication.kt`**
   ```kotlin
   class BillCraftApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           AnalyticsManager.init(this)
       }
   }
   ```

6. **Verify** by running the app and checking the Firebase console's **DebugView** (enable with `adb shell setprop debug.firebase.analytics.app com.billcraft.app`).

---

## 4. AdMob Setup

### 4.1 Create an AdMob account

1. Go to [admob.google.com](https://admob.google.com) and sign in with your Google account.
2. Complete the account setup (country, payment details).

### 4.2 Register BillCraft as an app

1. In the AdMob dashboard: **Apps → Add app**.
2. Select **Android**.
3. Search for your app on Play Store — if not published yet, click **"Add your app manually"**.
4. App name: `BillCraft – GST Invoice Maker`
5. Copy the **App ID** — it looks like `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`.

### 4.3 Create Ad Units

Create three ad units (one per format):

| Format | Name suggestion | Resulting Ad Unit ID format |
|--------|-----------------|----------------------------|
| Banner | `BillCraft-Banner-Invoice-List` | `ca-app-pub-XXX/YYY` |
| Interstitial | `BillCraft-Interstitial-Invoice-Created` | `ca-app-pub-XXX/YYY` |
| Rewarded | `BillCraft-Rewarded-Premium` | `ca-app-pub-XXX/YYY` |

### 4.4 Add the AdMob App ID to AndroidManifest.xml

```xml
<application ...>
    <!-- Replace with your real AdMob App ID -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY" />
</application>
```

> ⚠️ The `APPLICATION_ID` in the manifest is the **App ID** (with `~`), not the Ad Unit ID.

### 4.5 Initialise MobileAds in Application class

```kotlin
class BillCraftApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsManager.init(this)
        MobileAds.initialize(this) { initStatus ->
            Log.d("AdMob", "AdMob initialised: $initStatus")
        }
    }
}
```

### 4.6 Add the AdMob dependency (verify in `app/build.gradle.kts`)

```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-ads:23.0.0")
}
```

### 4.7 Add network security config to manifest

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
```

---

## 5. Build and Run

```bash
# Debug build (uses test ad IDs automatically)
./gradlew assembleDebug

# Install on connected device / emulator
./gradlew installDebug

# Or run from Android Studio:
# Click ▶ Run (Shift+F10)
```

### Emulator tips

- Use an x86_64 system image for best performance.
- Test ads will appear automatically in debug builds — no special setup needed.
- For Firebase Analytics `DebugView`, enable it with:
  ```bash
  adb shell setprop debug.firebase.analytics.app com.billcraft.app
  ```

---

## 6. Replace Test Ad IDs Before Release

> ⚠️ **Critical:** Shipping production builds with test ad IDs will result in **no revenue** and may violate AdMob policies.

**Steps:**

1. Open `app/src/main/java/com/billcraft/app/ads/AdConstants.kt`.
2. Replace the test ID constants with your real Ad Unit IDs:
   ```kotlin
   // Replace these:
   const val BANNER_AD_UNIT_ID       = "ca-app-pub-3940256099942544/6300978111"
   const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
   const val REWARDED_AD_UNIT_ID     = "ca-app-pub-3940256099942544/5224354917"

   // With your real IDs:
   const val BANNER_AD_UNIT_ID       = "ca-app-pub-YOUR_ID/YOUR_BANNER_UNIT"
   const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-YOUR_ID/YOUR_INTERSTITIAL_UNIT"
   const val REWARDED_AD_UNIT_ID     = "ca-app-pub-YOUR_ID/YOUR_REWARDED_UNIT"
   ```
3. Update `AndroidManifest.xml` with your real AdMob **App ID** (not Unit ID).
4. Rebuild and test on a physical device.

**Recommended: Use build variants**

Use `debug` variant for test IDs and `release` variant for real IDs:

```kotlin
// app/build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "ADMOB_BANNER_ID",
            "\"ca-app-pub-3940256099942544/6300978111\"")
    }
    release {
        buildConfigField("String", "ADMOB_BANNER_ID",
            "\"ca-app-pub-YOUR_ID/YOUR_BANNER_UNIT\"")
    }
}
```

Then reference `BuildConfig.ADMOB_BANNER_ID` instead of the hardcoded constant.

---

## 7. Keystore Setup for Release Signing

### 7.1 Generate a keystore

```bash
keytool -genkey -v \
  -keystore billcraft-release.jks \
  -alias billcraft-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

You will be prompted for passwords and identity information.

> ⚠️ **Back up your keystore file and passwords securely.** If you lose them, you cannot update your app on the Play Store.

### 7.2 Configure signing in `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../billcraft-release.jks")
            storePassword = System.getenv("KEYSTORE_PASS") ?: "your_store_password"
            keyAlias = "billcraft-key"
            keyPassword = System.getenv("KEY_PASS") ?: "your_key_password"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

> **Security best practice:** Store passwords in environment variables or `local.properties` (which is git-ignored), never in version control.

### 7.3 Build the release APK / AAB

```bash
# Android App Bundle (required for Play Store)
./gradlew bundleRelease

# APK (for direct distribution / testing)
./gradlew assembleRelease
```

Output locations:
- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

---

## 8. ProGuard / R8 Notes

The `app/proguard-rules.pro` file already contains rules for all major dependencies (Firebase, AdMob, Room, Hilt, ZXing, Kotlin, iText, Coil).

**Things to verify after enabling R8:**

1. Run a **release build** locally and test all major flows.
2. Check Logcat for any `ClassNotFoundException` or `NoSuchMethodException`.
3. If using **Retrofit** or custom serialization, ensure model classes are kept (already in `proguard-rules.pro`).
4. If you add new third-party libraries, check their documentation for required ProGuard rules and add them.

**Enable full R8 (recommended):**
```kotlin
// gradle.properties
android.enableR8.fullMode=true
```

---

## 9. Play Console Setup Checklist

Before submitting to the Google Play Store:

### App information
- [ ] App title: `BillCraft - GST Invoice Maker` (≤ 30 chars)
- [ ] Short description (≤ 80 chars) — see `STORE_LISTING.md`
- [ ] Full description — see `STORE_LISTING.md`
- [ ] App icon uploaded (512×512 px, PNG)
- [ ] Feature graphic uploaded (1024×500 px)
- [ ] At least 2 phone screenshots uploaded

### Technical
- [ ] Signed AAB uploaded (`app-release.aab`)
- [ ] Target SDK ≥ 33 (required for new apps since 2023)
- [ ] `google-services.json` present in `app/` (not in source control)
- [ ] Real AdMob App ID in `AndroidManifest.xml`
- [ ] Real Ad Unit IDs in `AdConstants.kt`

### Compliance
- [ ] Privacy Policy URL added (required for apps with ads)
- [ ] Content rating questionnaire completed
- [ ] Data safety form filled in (declare local storage, no data sharing)
- [ ] Ads declaration: `Contains ads` checked

### Testing
- [ ] Internal testing track created and tested
- [ ] Closed/open testing done (optional but recommended)

---

## 10. Testing Checklist

Run through the following manual tests before releasing:

### Onboarding
- [ ] Welcome screen loads, analytics event fires
- [ ] "Get Started" navigates to business setup
- [ ] "Already set up? Continue" navigates to dashboard

### Invoice
- [ ] Create invoice with multiple line items
- [ ] GST calculation correct (CGST + SGST for intra-state, IGST for inter-state)
- [ ] GSTIN validation accepts valid and rejects invalid formats
- [ ] Save as Draft, change status to Sent/Paid
- [ ] Share via WhatsApp (launches WhatsApp)
- [ ] Share as PDF (generates and opens PDF)
- [ ] Amount in words appears correctly

### Ads
- [ ] Banner ad appears on invoice list screen (test ad)
- [ ] Interstitial fires after every 5th invoice created (test ad)
- [ ] No crash when ad is not loaded yet
- [ ] Interstitial not shown twice within 60 seconds

### Payments
- [ ] Record cash payment — balance updates correctly
- [ ] Record UPI payment — transaction ID field appears
- [ ] Record cheque — cheque number field appears
- [ ] Invoice status changes to "Paid" when fully settled

### Offline
- [ ] Enable airplane mode — app works fully
- [ ] Create invoice, record payment, view customer — all work offline

### Analytics (Firebase DebugView)
- [ ] `onboarding_started` fires on first launch
- [ ] `invoice_created` fires with correct type/amount/item_count
- [ ] `invoice_shared` fires with correct share_method
- [ ] `pdf_generated` fires when PDF is created

### Localization
- [ ] Change device language to Hindi — Hindi strings appear
- [ ] All strings have translations (no English fallback where Hindi is expected)

---

*For questions or contributions, open an issue on the GitHub repository.*
