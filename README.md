# Budgie - Budget Tracker App

> A fully-featured Android budget tracking application built with Kotlin and Room DB.

<!-- GitHub Actions badge — update the path once your workflow file is added -->
<!-- ![Build Status](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/build.yml/badge.svg) -->

---

## Demo Video

> **[Click here to watch the demo video](PASTE_VIDEO_LINK_HERE)**
>
> *(Update this link once your video is uploaded and compressed)*

---

## Table of Contents

- [App Overview](#app-overview)
- [Team Members & Task Breakdown](#team-members--task-breakdown)
- [Features](#features)
- [Database Schema](#database-schema)
- [Setup & Installation](#setup--installation)
- [GitHub Actions](#github-actions)
- [References](#references)

---

## App Overview

Budgie is a personal finance and budget tracking Android app. Users can register, log in, create expense/income categories, log transactions, set monthly spending goals, and view reports — all stored locally using Room (SQLite).

**Tech Stack:**
- Language: Kotlin
- Database: Room (SQLite)
- Min SDK: API 24 (Android 7.0)
- Target SDK: API 36
- Architecture: Activity-based with LiveData observers

---

## Team Members & Task Breakdown

| Member | Student Number | Tasks |
|---|---|---|
| **Tayler** | ST10445063 | Room DB setup, User auth, Categories CRUD, Login/Dashboard |
| Person 2 | ST10XXXXXX | *(add your tasks here)* |
| Person 3 | ST10XXXXXX | *(add your tasks here)* |

---

## Features

### ✅ Person 1 — Authentication & Categories

#### User Registration & Login
- Users register with a **username, email, and password**
- Passwords are hashed using **SHA-256** before being stored — plain text passwords are never saved
- Login accepts **username or email** combined with the correct password
- Sessions are persisted using `SharedPreferences` so users stay logged in between app restarts
- A **Continue as Guest** option is available for users who do not want to register
- All user data is stored in the `users` Room DB table

**Relevant files:**
- `LoginActivity.kt` — login screen with real DB authentication
- `RegisterActivity.kt` — account creation with validation
- `utils/AuthUtils.kt` — shared SHA-256 hashing utility
- `data/entity/User.kt` — Room entity (id, username, email, passwordHash)
- `data/dao/UserDao.kt` — insert, getByEmail, getByUsername

#### Categories (Create / Edit / Delete)
- **8 default categories** are seeded into the database on first launch: Groceries, Transport, Dining, Entertainment, Utilities, Health, Shopping, Income
- Default categories are **read-only** — they cannot be edited or deleted
- Logged-in users can **add custom categories** specific to their account
- Custom categories can be **renamed or deleted** via an AlertDialog
- The Dashboard loads and displays all categories live from the database using **LiveData**
- A "Manage ›" button on the Dashboard navigates to the full categories management screen

**Relevant files:**
- `CategoriesActivity.kt` — full CRUD UI (RecyclerView + FAB + AlertDialog)
- `adapter/CategoryAdapter.kt` — RecyclerView adapter with edit/delete callbacks
- `data/entity/Category.kt` — Room entity (id, name, userId)
- `data/dao/CategoryDao.kt` — insert, update, delete, getLiveData by userId
- `data/AppDatabase.kt` — Room singleton, seeds default categories on `onCreate`
- `DashboardActivity.kt` — observes categories LiveData and renders chip list

---

### 🔲 Person 2 — *(Feature name here)*

> **Instructions for Person 2:** Replace this section with your feature documentation.
> Follow the same format as Person 1 above:
> - Short description of what the feature does
> - Any important implementation details (e.g. how photos are stored, what permissions are needed)
> - List of relevant files you created or modified

---

### 🔲 Person 3 — *(Feature name here)*

> **Instructions for Person 3:** Replace this section with your feature documentation.
> - Short description of what the feature does
> - Any important implementation details
> - List of relevant files you created or modified

---

## Database Schema

### `users` table

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| username | TEXT | Unique |
| email | TEXT | Unique |
| passwordHash | TEXT | SHA-256 hash — never plain text |

### `categories` table

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| name | TEXT | Category label |
| userId | INTEGER | 0 = global default, >0 = user-specific |

> *(Other team members: add your table schemas here as you build them)*

---

## Setup & Installation

### Prerequisites
- Android Studio Ladybug or newer
- JDK 11+
- Android device or emulator running API 24+

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
   ```

2. **Open in Android Studio**
   - File → Open → select the `Budgie_BudgetApp` folder

3. **Sync Gradle**
   - Android Studio will prompt you to sync — click **Sync Now**
   - Required dependencies (Room, KSP, Coroutines) will download automatically

4. **Run the app**
   - Select an emulator or connected device
   - Click the **Run** button (▶)

5. **First launch**
   - The database is created automatically on first run
   - Default categories (Groceries, Transport, etc.) are seeded immediately
   - Register a new account or tap **Continue as Guest**

---

## GitHub Actions

Automated build and test CI is configured using GitHub Actions.

> *(Once the workflow file is added at `.github/workflows/build.yml`, replace the badge at the top of this README with your actual repo path)*

References used to set up CI:
- https://github.com/marketplace/actions/automated-build-android-app-with-github-action [Accessed 03 November 2025]
- https://github.com/IMAD5112/Github-actions/blob/main/.github/workflows/build.yml [Accessed 03 November 2025]

---

## References

> **All team members: add your references here as you complete your tasks. Use Harvard or IEEE format as required by your institution.**

- Android Developers. (2024). *Room persistence library*. https://developer.android.com/training/data-storage/room
- Android Developers. (2024). *Kotlin coroutines on Android*. https://developer.android.com/kotlin/coroutines
- Android Developers. (2024). *LiveData overview*. https://developer.android.com/topic/libraries/architecture/livedata
- Android Developers. (2024). *RecyclerView*. https://developer.android.com/develop/ui/views/layout/recyclerview
- OWASP. (2024). *Password storage cheat sheet*. https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html

---

*Built for PROG7313 — Open Source Coding — Year 3, Semester 5*
