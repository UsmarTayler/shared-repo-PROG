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
| **Matthew Jackson** | ST1010445830 | Expense entry form (date, time, description, category, amount), photo capture (camera + gallery), validation, DB save 
| **Zain Labuschagne** | ST10442835 | Transaction history with date filtering, expense totals per category, monthly budget goals, dashboard budget indicators |

---

## Features

### ✅ Tayler Usmar — Authentication & Categories

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

### ✅ Matthew Jackson — Expense Entry & Photo Capture

#### Expense Entry Form
- Built a full expense/income entry form with **amount, category (spinner), date picker, start/end time pickers, and description**
- Transaction type (expense or income) is toggled via tab buttons
- All fields are **validated** before saving — empty amount, invalid number, missing description, and unselected category are all caught
- Users can **add a new category inline** from within the form without navigating away
- Completed transactions are saved to the `transactions` Room DB table via `TransactionDao`

#### Photo Capture
- Users can attach a photo by **taking a new photo with the camera** or **choosing from the gallery**
- Camera permission (`CAMERA`) is requested at runtime using `ActivityResultContracts.RequestPermission`
- Camera photos are saved to `MediaStore` then copied to **internal app storage** so they persist independently of the gallery
- A photo preview is shown immediately after capture; users can remove the photo before saving
- The saved file path is stored in the transaction record

**Relevant files:**
- `TransactionsActivity.kt` — expense/income form with validation, date/time pickers, inline category creation, and photo capture
- `data/entity/Transaction.kt` — Room entity (id, amount, categoryId, categoryName, type, description, date, startTime, endTime, photoPath, userId)
- `data/dao/TransactionDao.kt` — insert, delete, getByDateRange, getTotalByType, getCategoryTotals
- `data/Converters.kt` — Room type converters for `Date` fields
- `AndroidManifest.xml` — added `CAMERA` permission

---

### ✅ Zain Labuschagne — History, Reports & Budget Goals

#### Transaction History
- Displays all transactions **grouped by date** with "Today" / "Yesterday" relative labels
- Filter by **Day, Week, or Month** using chip buttons; date range is shown above the list
- **Live search** filters transactions by category name, description, or amount as the user types
- Tap a transaction to see full details; if a photo is attached it can be viewed in a dialog
- Transactions can be **deleted** with a confirmation prompt; the associated photo file is also removed from storage

**Relevant files:**
- `HistoryActivity.kt` — filterable, searchable history list with detail view and delete
- `utils/DateRangeHelper.kt` — calculates start/end date boundaries for Day/Week/Month ranges

#### Reports
- Shows **total income and total expenses** for the selected period (Day/Week/Month spinner)
- Breaks down expenses **per category** with the actual amount spent
- Each category card compares spending against its budget goals — a **progress bar** tracks max budget usage
- Status indicators warn when spending hits 80% or exceeds 100% of the max budget, or falls below the min budget
- "Set Budget Goals" button navigates directly to `BudgetGoalsActivity`
- Modified `DashboardActivity` to display **live budget indicators** showing totals vs goals

**Relevant files:**
- `ReportsActivity.kt` — summary + category breakdown with budget progress bars
- `DashboardActivity.kt` — updated to show real transaction totals and budget status indicators

#### Monthly Budget Goals
- Users set a **minimum and/or maximum budget per category per month**
- Navigate between months using prev/next arrow buttons
- Each category shown as a card; tapping opens a dialog to **add, edit, or delete** that month's budget
- Min and max are independently optional — either or both can be set per category
- Budget data stored in the `budget_goals` Room DB table

**Relevant files:**
- `BudgetGoalsActivity.kt` — monthly budget management UI with per-category cards
- `data/entity/BudgetGoal.kt` — Room entity (id, categoryId, categoryName, minBudget, maxBudget, userId, month, year)
- `data/dao/BudgetGoalDao.kt` — insert, delete, getGoalsForMonth, getGoalForCategory

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
