# Daily Tasks

A simple native Android to-do list built with Kotlin and Jetpack Compose. Tasks are saved on-device using `SharedPreferences`, so your list survives app restarts without requiring an account or network connection.

## Features

- Add tasks using the button or the keyboard's Done action.
- Mark tasks complete and remove individual tasks.
- Filter between all, active, and completed tasks.
- Clear all completed tasks at once.
- Persist task titles and completion state locally as JSON in `SharedPreferences`.

## Build and run

Open this project in Android Studio and let Gradle sync. Run the `app` configuration on an Android device or emulator (Android 6.0 / API 23 or newer).

The app does not request network access and stores data only on the device.
