# Dicoding Story App

Dicoding Story App is an Android application developed as a final submission for the **Belajar Pengembangan Aplikasi Android Intermediate** course, part of the **MBKM Bangkit 2024 Batch 2** program.

This application allows users to register, log in, view and share stories with others. It implements modern Android development practices including Jetpack components, custom views, local data persistence, network requests, animations, unit testing, and Paging 3.

## Features

- ✅ User Registration
  - Users can register a new account with proper form validation.
  
- ✅ User Login
  - Secure login with token-based authentication.
  - Login session is saved locally using `DataStore Preferences`.

- ✅ Custom Views
  - Includes custom EditText components created using `CustomView`.

- ✅ Session Management
  - User authentication token is securely stored in preferences.
  - Logout functionality is available to clear the session.

- ✅ Story Feed
  - List of stories retrieved from API and displayed using RecyclerView.
  - Supports infinite scrolling with **Paging 3** library.

- ✅ Story Details
  - Tapping on a story item opens a detail screen with complete information.

- ✅ Add New Story
  - Users can upload new stories with an image and description.
  - Supports uploading location (optional).

- ✅ Maps Integration
  - Stories with location metadata are displayed on Google Maps.

- ✅ Animation
  - UI animations enhance the user experience with smooth transitions.

- ✅ Unit Testing
  - Core components are covered with unit tests to ensure functionality.

## 📸 Screenshots

### 🔐 SignUp and Login Screen
<p align="center">
  <img src="https://github.com/user-attachments/assets/d08f35fa-e2c1-4650-922a-88bac6afcb12" width="40%" />
  <img src="https://github.com/user-attachments/assets/ac8bdd50-47dc-4755-b00b-d20fd198e032" width="40%" />
</p>

### 📜 Story List and Story Detail Screen
<p align="center">
  <img src="https://github.com/user-attachments/assets/5b0978b4-847f-46e7-9050-1c2926aced00" width="40%" />
  <img src="https://github.com/user-attachments/assets/e551aa0b-3a89-4d58-87fa-35d6e5d12222" width="40%" />
</p>

### ➕ Add Story Screen
<p align="center">
  <img src="https://github.com/user-attachments/assets/aafc3f3a-8656-430a-8705-63ebc543f1f9" width="28%" />
  <img src="https://github.com/user-attachments/assets/391dbbe3-8b09-47a3-94fc-14dee9655409" width="28%" />
  <img src="https://github.com/user-attachments/assets/43830d7f-0e02-4cba-a6b1-e454debad603" width="28%" />
</p>

### 🗺️ Map Screen
<p align="center">
  <img src="https://github.com/user-attachments/assets/ff376a4b-34c8-4313-bbc2-4b8344543765" width="28%" />
  <img src="https://github.com/user-attachments/assets/c8916f72-eff1-434d-89f8-9e58db583f05" width="28%" />
  <img src="https://github.com/user-attachments/assets/223145b1-3154-44f6-b418-3523fbdb73ef" width="28%" />
</p>

## Tech Stack

- Kotlin
- Retrofit2 + OkHttp
- Jetpack ViewModel + LiveData
- Jetpack Navigation Component
- Paging 3
- Google Maps SDK
- DataStore Preferences
- Coroutine & Flow
- Glide (for image loading)
- Custom View
- Unit Testing with JUnit and Mockito

## Getting Started

To build and run this project:

1. Clone this repository
2. Open the project in Android Studio
3. Set up your API key for Google Maps (if required)
4. Run on a physical device or emulator with minimum SDK 21+

## Acknowledgments

This project is part of the final submission for the Android Intermediate Learning Path by Dicoding and Bangkit Academy 2024. Special thanks to the mentors, reviewers, and the Dicoding team.

---

> For any feedback or suggestions, feel free to open an issue or contact me via email at **zahraaalfiya@gmail.com**.

