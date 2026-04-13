# SetNote

SetNote is a minimalist Android application designed for tracking workout sessions. It focuses on speed and structured data entry, allowing users to log their sets quickly without unnecessary distractions.

---

## Key Features

*   Rapid Session Logging: A streamlined editor to add exercises and sets during a workout.
*   Workout Templates: Save routines as templates to start future sessions faster.
*   Session Archive: A history of past performance organized for easy review.
*   PDF Export: Generate professional summaries of workout sessions for personal backup or sharing.
*   Dynamic Theme Support: Seamlessly switches between light and dark modes based on system preferences.

---

## Tech Stack and Architecture

I used official Android Jetpack libraries to ensure the app is stable and follows modern development standards:

*   Language: Java
*   Database: Room Persistence Library for local data storage.
*   Navigation: Jetpack Navigation component for a smooth, single-activity fragment flow.
*   UI Components: Material Design 3, RecyclerView for dynamic lists, and CardView.
*   Architecture: MVVM (Model-View-ViewModel) to separate data logic from the user interface.

---

## Technical Implementation and Learning

### 1. Database Management with Room
I implemented a local database to store workout sessions, exercises, and individual sets. This involved setting up relationships between data entities so that workout history is preserved and easily accessible within the app.

### 2. Document Generation (PDF Export)
One of the more complex features was implementing a PDF export function. I used the Android `PdfDocument` API to draw workout data onto a canvas. I also integrated a `FileProvider` to securely share the generated PDF files with other apps.

### 3. Responsive UI and Theming
I focused on creating a clean, professional user interface. I implemented a `ThemeHelper` class to manage light and dark mode transitions and used Material Design components to ensure a consistent look and feel across different Android devices.

---

## Getting Started

### Prerequisites
*   Android Studio Ladybug or newer.
*   Android SDK 35 (Compile SDK).
*   Minimum Android Version: 7.0 (API Level 24).

### Building from Source
1.  Clone the repository:
    `git clone https://github.com/arti405/SetNote.git`
2.  Open the project in Android Studio.
3.  Sync the project with Gradle files.
4.  Run the `app` module on an emulator or physical device.