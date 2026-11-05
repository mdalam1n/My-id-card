# My Personal Document App

A secure, offline-first Android application built using **Kotlin** and **Jetpack Compose** (Material 3 UI). This app allows users to store, manage, and view their personal identity documents privately and securely on their local device.

## Features

- **Home Dashboard:** Custom profile picture setup, editable user name, and quick access to various document categories.
- **Document Categories:**
  - **My Ekama (ID):** Front and back side image viewing with full-screen zoom support.
  - **My Driving Licence:** Dual image slots (Front & Back) with full-screen preview.
  - **My Home ID:** Secure storage for local identification cards.
  - **My Passport:** Passport document management with front/back or page view support.
  - **Personal Card's:** Support for up to 5 custom personal card slots with add, update, replace, and delete options.
- **100% Offline & Private:** All images and profile data are stored strictly inside the app's internal private storage directory (`context.filesDir`)[span_1](start_span)[span_1](end_span). No cloud uploads or external servers are used, ensuring complete privacy.
- **Full-Screen Zoomable Preview:** Tap on any document image to open an interactive full-screen viewer with zoom and pan controls[span_2](start_span)[span_2](end_span).

## Tech Stack

- **Language:** Kotlin[span_3](start_span)[span_3](end_span)
- **UI Framework:** Jetpack Compose (Material 3)[span_4](start_span)[span_4](end_span)
- **Storage:** Local Internal Storage (App-private directory)[span_5](start_span)[span_5](end_span)

## Getting Started

1. Clone or download this repository as a ZIP file[span_6](start_span)[span_6](end_span).
2. Open the project in **Android Studio**.
3. Build and run the app on your Android device or emulator.
