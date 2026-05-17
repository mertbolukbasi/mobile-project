<div align="center">

# 🌌 Paginex

**A Cosmic Book Tracking & Social Discovery App**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09.00-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-FFCA28.svg?logo=firebase)](https://firebase.google.com/)

</div>

---

## 📖 About The Project

**Paginex** is a modern, beautifully designed Android application tailored for book lovers. It goes beyond simple book tracking by introducing a unique "Cosmic" visual theme where your favorite books orbit your profile like planets in a galaxy. 

Discover new books through the Open Library API integration, share your reviews, curate custom booklists, and connect with a vibrant community of readers.

### ✨ Key Features

- **🚀 Cosmic UI/UX**: A stunning dark-mode-first design featuring animated galaxies, orbits, and neon accents.
- **📚 Book Discovery**: Integrated with the Open Library API to search and add thousands of books instantly.
- **📊 Reading Tracking**: Keep track of what you're reading, want to read, or have completed.
- **🗣️ Social Feed & Reviews**: Rate books, write reviews, and interact with other readers via likes and comments.
- **📑 Custom Booklists**: Create public or private collections of books to share or keep for yourself.
- **👤 Interactive Profiles**: Showcase your personal "Galaxy" of favorite books on your profile.

---

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture & Navigation:** Jetpack Navigation Compose
- **Backend & Database:** Firebase (Auth, Firestore, Storage)
- **Image Loading:** Coil
- **External API:** Open Library Search API

---

## 🚀 How to Run the Project

Follow these steps to get Paginex running on your local machine:

### Prerequisites

1. **Android Studio**: Download and install the latest stable version of [Android Studio](https://developer.android.com/studio).
2. **Java Development Kit (JDK)**: JDK 17 is required (this is usually bundled with recent versions of Android Studio).
3. **Android SDK**: Ensure Android SDK Platform **34** is installed via the SDK Manager.

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/paginex.git
   cd paginex
   ```

2. **Open the project**
   - Launch Android Studio.
   - Select **File > Open...** and navigate to the `paginex` root directory.
   - Wait for Android Studio to index the files and download Gradle dependencies.

3. **Firebase Configuration**
   - The app uses Firebase. Make sure `google-services.json` is located in the `app/` directory (if you are the owner, it might already be there or you might need to generate it from your Firebase console).

4. **Run the application**
   - Connect an Android device via USB (with USB Debugging enabled) OR start an Android Virtual Device (AVD) from the Device Manager.
   - Click the ▶️ **Run 'app'** button in Android Studio (or press `Shift + F10`).

> **Note:** The `local.properties` file is intentionally excluded from version control. Android Studio will automatically generate it with your local SDK path upon opening the project.