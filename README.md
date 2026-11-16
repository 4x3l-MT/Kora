# Kora

A privacy-first, 100% offline menstrual cycle and biometric health tracker built for Android.

Kora is engineered around strict data sovereignty: sensitive health records remain exclusively in the device's private internal storage without cloud servers, external APIs, user accounts, or third-party trackers.

---

## Key Features

* **100% Offline & Privacy-First:** Zero network permissions or cloud dependencies. All data is stored locally in a private relational database (`kora_local_period.db`).
* **Biometric Cycle Prediction Engine:**
  * **Weighted Moving Average:** Calculates expected cycle length giving higher weight to more recent cycles.
  * **Dynamic Luteal Phase Adjustment:** Personalizes luteal phase duration (11 to 14 days) using logged biomarkers (LH strips, basal body temperature, cervical mucus, and ovulation pain).
  * **Anomaly Detection & Confidence Intervals:** Evaluates cycle variability using standard deviation to dynamically widen fertile windows when cycle irregularity exceeds 4 days.
* **Daily Symptom & Biomarker Logging:** Track flow intensity, physical symptoms, mood, energy levels, and fertility indicators.
* **Minimalist Material 3 UI:** Reactive, dark-mode-ready interface built with Jetpack Compose for speed, clarity, and discretion.

---

## Tech Stack

* **Language:** Kotlin 2.1.10
* **UI Framework:** Jetpack Compose with Material Design 3 & Material Icons Extended
* **Architecture:** MVVM (Model-View-ViewModel)
* **Concurrency & State:** Kotlin Coroutines & `StateFlow`
* **Navigation:** Jetpack Navigation Compose
* **Local Storage:** Native SQLite (`SQLiteOpenHelper`)
* **Date & Biometric Logic:** `java.time` API (`LocalDate`, `YearMonth`, `ChronoUnit`) & statistical modeling
* **Build System & Testing:** Android Gradle Plugin (AGP 9.4.1), Kotlin DSL (`build.gradle.kts`), Version Catalog (`libs.versions.toml`), and JUnit 4

---

## Getting Started

### Prerequisites
* Android Studio Ladybug (or newer)
* JDK 17+
* Android SDK 26+ (Android 8.0 Oreo or higher)

### Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/4x3l-MT/Kora.git](https://github.com/4x3l-MT/Kora.git)
   ```

2. **Open in Android Studio:**
   * Launch Android Studio and select **Open**.
   * Navigate to the cloned `Kora` directory and allow Gradle to sync dependencies.

3. **Run the app:**
   * Select an emulator or connect a physical Android device via ADB.
   * Click **Run** (`Shift + F10`).

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
