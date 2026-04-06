# 💰 Finance — Personal Finance Companion App

<div align="center">

![Finance App Banner](https://img.shields.io/badge/Finance-Personal%20Companion-1D9E75?style=for-the-badge&logo=android&logoColor=white)

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen?style=flat-square&logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0.21-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange?style=flat-square)](LICENSE)
[![API](https://img.shields.io/badge/Min%20SDK-26-red?style=flat-square)](https://android.com)
[![Build](https://img.shields.io/badge/Build-Passing-success?style=flat-square)]()

<br/>

> **A modern, intelligent personal finance companion built for everyday use.**  
> Track transactions, monitor budgets, understand your spending patterns,  
> and receive AI-powered financial tips — all in one beautifully designed app.

<br/>

[Features](#-features) • [Screenshots](#-screenshots) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack) • [Setup](#-setup--installation) • [API Layer](#-mock-api-layer) • [Project Structure](#-project-structure)

</div>

---

## 📖 About The App

**Finance** is a lightweight personal finance companion app designed to help users understand their daily money habits in a simple, engaging, and visually rich way. It is **not** a banking app — it is a smart tracking tool that puts financial awareness in your hands.

The app was built with a strong focus on **product thinking, mobile UX, and engineering best practices**. Every feature is thoughtfully integrated to serve real user needs — from tracking a morning coffee expense to understanding whether you'll hit your savings goal by the end of the month.

### 🎯 Purpose

Most people know they should track their money. Very few actually do — because existing tools are either too complex, too minimal, or too boring. Finance solves this by being:

- **Fast to use** — Add a transaction in under 10 seconds
- **Visually rewarding** — Animated charts and progress bars make finances feel alive
- **Intelligent** — AI-powered tips and spending forecasts give context, not just data
- **Secure** — Biometric lock ensures your financial data stays private
- **Offline-first** — Works entirely without internet; the API layer enhances but never blocks

---

## ✨ Features

### 🏠 Home Dashboard
- **Live balance card** — real-time total balance with a gradient hero design
- **Monthly income & expense** summary cards with compact currency formatting
- **Savings rate progress bar** — animated, shows percentage of income saved this month
- **Financial Health Score** — composite score (0–100) across savings, budget adherence, and tracking consistency with an animated arc ring and sub-score breakdown
- **Month-End Spending Forecast** — linear projection of where you'll end up based on daily average, with a savings estimate and on-track / over-budget indicator
- **Spending breakdown by category** — animated horizontal bars per category with percentages
- **Recent activity list** — last 5 transactions with quick navigation to full list
- **Shimmer loading effect** — skeleton placeholder while data loads

### 💳 Transaction Tracking
- **Add transactions** — full form with amount, type (income/expense), category grid, date picker, and optional note
- **Edit transactions** — tap any transaction card to open pre-filled edit form
- **Delete transactions** — swipe left to delete with an **undo snackbar** (5-second window)
- **Filter by type** — All / Income / Expenses filter chips
- **Full-text search** — searches across title, note, and category name simultaneously
- **Grouped by date** — transactions grouped under Today / Yesterday / formatted date headers
- **12 pre-built categories** — Food & Dining, Transport, Shopping, Health, Entertainment, Bills, Salary, Education, Travel, Investments, Gifts, Other
- **Form validation** — inline error messages for all required fields

### 🎯 Monthly Budget Goals
- **Set budget per category** — custom spending limit for any category for the current month
- **Edit goal amount** — update the budget limit without losing progress data
- **Live animated progress bars** — real-time spend vs budget with smooth animation
- **Smart alerts** — yellow warning banner at 80% of budget, red banner when exceeded
- **Overall budget summary card** — aggregate view across all goals for the month
- **Duplicate prevention** — enforces one goal per category per month
- **Delete goals** — remove any goal with a single tap

### 📊 Insights Screen
- **7-day daily bar chart** — MPAndroidChart-powered bar chart showing last 7 days of spending
- **Week vs last week comparison** — total spend with percentage change and trend icon
- **Month vs last month comparison** — current vs previous month with % delta badge
- **Top spending category** — highlighted card showing where most money went
- **Full category breakdown** — animated percentage bars for every expense category
- **AI Financial Tips** — 5 personalised tips fetched from the mock Finance API with a "Powered by Finance API" badge, 600ms loading state, and category-coloured tip cards

### 🔐 Security
- **Biometric lock** — fingerprint / PIN authentication on every app launch
- **Toggle in settings** — enable or disable biometric lock at any time
- **Graceful fallback** — falls back to device PIN / password if biometric is unavailable

### ⚙️ Profile & Settings
- **Display name** — personalise the app with your name (shown on home screen greeting)
- **Currency selection** — 8 supported currencies (INR, USD, EUR, GBP, JPY, AED, SGD, CAD) with live exchange rates fetched from the mock API
- **Dark mode toggle** — instant theme switch with system-level persistence via DataStore
- **Biometric lock toggle** — enable / disable from settings
- **Daily reminder toggle** — enable a 9 PM push notification to log expenses
- **Export transactions** — share all transactions as a CSV file via any app (Gmail, Drive, WhatsApp)
- **About section** — app version, tech stack, API info

### 🔔 Smart Notifications
- **Daily reminder** — WorkManager-scheduled notification at 9 PM to log daily expenses
- **Budget alerts** — push notification when any category hits 80% or exceeds 100% of budget
- **Context-aware messages** — notification text adapts based on how much of income has been spent this month

### 🌙 Additional
- **Dark mode** — complete dark theme with carefully tuned color palette
- **Animated screen transitions** — slide + fade transitions between all screens
- **Offline-first** — all core features work without internet; API features degrade gracefully
- **Demo data seeded on first install** — 12 categories and 10 transactions pre-loaded so every screen has content immediately
- **Edge-to-edge design** — respects system insets for a modern, immersive look

---

## 📱 Screenshots

| Home (Light) | Home (Dark) | Transactions | Add Transaction |
|:---:|:---:|:---:|:---:|
|<img src="assets/HomeScreen - Light Mode.jpeg" alt="Screen 1" width="250"/>| *(screenshot)* | *(screenshot)* | *(screenshot)* |

| Goals | Insights | AI Tips | Settings |
|:---:|:---:|:---:|:---:|
| *(screenshot)* | *(screenshot)* | *(screenshot)* | *(screenshot)* |

| Biometric Lock | Health Score | Forecast | Export |
|:---:|:---:|:---:|:---:|
| *(screenshot)* | *(screenshot)* | *(screenshot)* | *(screenshot)* |

> 📹 **Demo Video:** [Watch on Loom / Google Drive](#) *(add your link here)*

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with a **Repository pattern** and **Hilt dependency injection** throughout. Every layer has a single responsibility.

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                          │
│         Jetpack Compose Screens + ViewModels         │
│  HomeScreen │ TransactionScreen │ GoalsScreen │ etc  │
└──────────────────────┬──────────────────────────────┘
                       │ observes StateFlow
┌──────────────────────▼──────────────────────────────┐
│                 ViewModel Layer                      │
│  HomeViewModel │ TransactionViewModel │ GoalViewModel│
│  InsightsViewModel │ SettingsViewModel               │
│  • Business logic  • State management (StateFlow)   │
│  • Coroutine orchestration                          │
└──────────────────────┬──────────────────────────────┘
                       │ suspend functions / Flow
┌──────────────────────▼──────────────────────────────┐
│               Repository Layer                       │
│  TransactionRepository │ GoalRepository              │
│  CategoryRepository    │ ApiRepository               │
│  • Single source of truth                           │
│  • Abstracts local DB and remote API                │
└──────────┬───────────────────────────┬──────────────┘
           │                           │
┌──────────▼──────────┐   ┌───────────▼──────────────┐
│    Local DB Layer    │   │    Remote API Layer       │
│  Room (SQLite)       │   │  Retrofit + OkHttp        │
│  • TransactionDao    │   │  • MockApiInterceptor     │
│  • GoalDao           │   │  • FinanceApiService      │
│  • CategoryDao       │   │  • Exchange rates         │
│  • AppDatabase       │   │  • Financial tips         │
└─────────────────────┘   └──────────────────────────┘
```

### Key Architecture Decisions

**Why MVVM + Repository?**  
Clean separation of UI, business logic, and data. ViewModels survive configuration changes. Repositories make data sources swappable — switching from mock to real API requires changing only one file.

**Why Room with Flow?**  
All DAO queries return `Flow<T>`. Any database change instantly propagates to all observing screens with zero manual refresh calls. Adding a transaction on the Transactions screen updates the Home dashboard balance in real time automatically.

**Why Hilt?**  
Constructor injection throughout — every ViewModel, Repository, and Worker gets its dependencies injected. No manual instantiation anywhere. Makes the codebase testable and the dependency graph explicit.

**Why Mock API instead of a real backend?**  
The mock API layer demonstrates the full production network architecture — Retrofit service interface, OkHttp interceptor, Repository abstraction, sealed `ApiResult` error handling, and loading states — without requiring a real server. Swapping `MockApiInterceptor` for a real base URL is literally the only change needed for production.

**Offline-first by design**  
All core features (transactions, goals, insights, balance) run entirely on local Room data. The app works with no internet connection. API features (tips, exchange rates) fail silently — the UI shows nothing rather than an error screen. Core features never depend on network availability.

---

## 🛠️ Tech Stack

| Category | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Kotlin | 2.0.21 | Primary language |
| **UI Framework** | Jetpack Compose | BOM 2024.12.01 | Declarative UI |
| **UI Components** | Material Design 3 | via BOM | Design system |
| **Architecture** | MVVM + Repository | — | App architecture |
| **DI** | Hilt (Dagger) | 2.51.1 | Dependency injection |
| **DI KSP** | KSP | 2.0.21-1.0.28 | Annotation processing |
| **Database** | Room (SQLite) | 2.6.1 | Local persistence |
| **Async** | Kotlin Coroutines | 1.8.1 | Async operations |
| **Reactive** | Kotlin Flow | 1.8.1 | Reactive data streams |
| **Navigation** | Navigation Compose | 2.8.4 | Screen navigation |
| **Networking** | Retrofit 2 | 2.11.0 | API layer |
| **HTTP Client** | OkHttp 4 | 4.12.0 | HTTP + mock interceptor |
| **JSON** | Gson | 2.11.0 | JSON serialisation |
| **Charts** | MPAndroidChart | v3.1.0 | Bar chart visualisation |
| **Preferences** | DataStore | 1.1.1 | Settings persistence |
| **Background** | WorkManager | 2.9.1 | Scheduled notifications |
| **Biometric** | AndroidX Biometric | 1.1.0 | Fingerprint / PIN auth |
| **Splash Screen** | Core SplashScreen | 1.0.1 | Launch screen |
| **Build System** | Gradle (KTS) | 8.7.3 | Build tooling |
| **Min SDK** | Android 8.0 (API 26) | — | Device support |
| **Target SDK** | Android 15 (API 35) | — | Latest platform |

---

## 📡 Mock API Layer

The app implements a complete network architecture layer using a **custom OkHttp interceptor** that simulates a real REST API with realistic 600ms network latency.

### Endpoints

| Endpoint | Method | Description | UI Location |
|---|---|---|---|
| `/v1/exchange-rates` | GET | Currency exchange rates (8 currencies) | Settings → Currency sheet |
| `/v1/financial-tips` | GET | 5 AI-generated financial tips | Insights → AI Tips card |
| `/v1/spending-advice?percent=N` | GET | Context-aware spending advice | Internal / Logcat |

### How to Verify It's Working

**Method 1 — Visual proof**
- Go to **Settings → Currency** → bottom sheet opens with 600ms spinner → rates populate
- Go to **Insights** → scroll to bottom → AI Tips card appears after 600ms spinner

**Method 2 — Logcat**
```
Filter: OkHttp
```
You'll see full request/response logs including the JSON body and timing.

**Method 3 — Network Inspector**
Android Studio → App Inspection → Network Inspector → all 3 endpoints listed with 200 status and ~600ms response time.

### Switching to a Real API
The interceptor is designed to be removed in one step:

```kotlin
// In NetworkModule.kt — remove this line:
.addInterceptor(MockApiInterceptor())

// And update:
.baseUrl("https://your-real-api.com/v1/")
```

Everything else — the service interface, repositories, ViewModels, and UI — requires zero changes.

---

## 🗂️ Project Structure

```
app/src/main/java/com/yourname/financeapp/
│
├── 📁 data/
│   ├── 📁 local/
│   │   ├── AppDatabase.kt          # Room DB singleton + seeding callback
│   │   ├── Converters.kt           # TypeConverter for TransactionType enum
│   │   ├── TransactionDao.kt       # 12 queries — CRUD, search, aggregates
│   │   ├── GoalDao.kt              # Goal CRUD + month/year queries
│   │   └── CategoryDao.kt          # Category CRUD
│   │
│   ├── 📁 model/
│   │   ├── Transaction.kt          # @Entity + TransactionType enum
│   │   ├── Category.kt             # @Entity + DEFAULT_CATEGORIES list
│   │   ├── Goal.kt                 # @Entity with FK to Category
│   │   ├── TransactionWithCategory.kt  # @Relation join model
│   │   ├── GoalWithCategory.kt     # @Relation join model
│   │   └── CategorySpending.kt     # Aggregate projection for charts
│   │
│   ├── 📁 preferences/
│   │   └── UserPreferences.kt      # DataStore wrapper for all settings
│   │
│   ├── 📁 remote/
│   │   ├── ApiService.kt           # Retrofit interface (3 endpoints)
│   │   ├── ApiModels.kt            # Response data classes
│   │   ├── ApiRepository.kt        # Sealed ApiResult + safeCall wrapper
│   │   └── MockApiInterceptor.kt   # OkHttp interceptor with mock JSON
│   │
│   └── 📁 repository/
│       ├── TransactionRepository.kt
│       ├── GoalRepository.kt
│       └── CategoryRepository.kt
│
├── 📁 di/
│   ├── DatabaseModule.kt           # Hilt: Room DB + all DAOs
│   └── NetworkModule.kt            # Hilt: OkHttp + Retrofit + ApiService
│
├── 📁 ui/
│   ├── 📁 theme/
│   │   ├── Color.kt                # Brand colors + category palette (10 colors)
│   │   ├── Theme.kt                # Light/Dark MaterialTheme configuration
│   │   └── Type.kt                 # Typography scale
│   │
│   ├── 📁 navigation/
│   │   └── AppNavigation.kt        # NavHost + bottom bar + route constants
│   │
│   ├── 📁 components/              # Reusable Compose components
│   │   ├── TransactionCard.kt      # Swipeable card with delete + edit
│   │   ├── SummaryCard.kt          # Income / expense summary card
│   │   ├── CategoryChip.kt         # Filter chip component
│   │   ├── EmptyState.kt           # Empty state with icon + CTA
│   │   ├── ShimmerEffect.kt        # Loading skeleton for Home screen
│   │   └── SnackbarController.kt   # Centralized snackbar with undo support
│   │
│   ├── 📁 lock/
│   │   ├── LockScreen.kt           # Biometric lock UI with gradient
│   │   └── BiometricHelper.kt      # BiometricPrompt wrapper
│   │
│   ├── 📁 home/
│   │   ├── HomeScreen.kt           # Dashboard with all summary cards
│   │   ├── HomeViewModel.kt        # Balance, income, expense, score, forecast
│   │   └── HealthScoreCard.kt      # Animated arc health score component
│   │
│   ├── 📁 transactions/
│   │   ├── TransactionListScreen.kt    # List with search, filter, swipe-delete
│   │   ├── AddEditTransactionScreen.kt # Full transaction form
│   │   └── TransactionViewModel.kt     # CRUD, filter, search, validation
│   │
│   ├── 📁 goals/
│   │   ├── GoalsScreen.kt          # Goal list + Add/Edit bottom sheets
│   │   └── GoalViewModel.kt        # Goal progress, alerts, budget calc
│   │
│   ├── 📁 insights/
│   │   ├── InsightsScreen.kt       # Charts + comparisons + AI tips
│   │   ├── InsightsViewModel.kt    # Weekly/monthly comparisons + API tips
│   │   └── ForecastCard.kt         # Month-end spending projection card
│   │
│   └── 📁 settings/
│       ├── SettingsScreen.kt       # Full settings + profile UI
│       └── SettingsViewModel.kt    # Preferences + API calls
│
├── 📁 util/
│   ├── CurrencyFormatter.kt        # Multi-currency formatting with symbol map
│   ├── DateFormatter.kt            # Relative dates, chart labels, epoch utils
│   └── Constants.kt                # App-wide constants
│
├── 📁 workers/
│   ├── DailyReminderWorker.kt      # WorkManager: 9 PM daily notification
│   └── BudgetAlertWorker.kt        # WorkManager: budget threshold alerts
│
├── FinanceApp.kt                   # @HiltAndroidApp + WorkManager config
└── MainActivity.kt                 # AppCompatActivity + biometric gate
```

---

## 🚀 Setup & Installation

### Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Ladybug / Meerkat (2024.2+) |
| JDK | 17 |
| Android SDK | API 35 (compile), API 26 (min) |
| Gradle | 8.7.3 |

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/finance-app.git
cd finance-app

# 2. Open in Android Studio
# File → Open → select the cloned folder

# 3. Wait for Gradle sync
# First sync downloads all dependencies including MPAndroidChart from JitPack
# This may take 2–3 minutes on first run

# 4. Run on device or emulator
# Shift + F10  or  click the green Run button
# Requires Android API 26+ (Android 8.0 Oreo or higher)
```

### First Launch Experience

On first install, the app automatically:
1. Creates the Room database
2. Seeds **12 default categories** with icons and colors
3. Seeds **10 demo transactions** spread across the past 10 days
4. All 5 screens have meaningful content immediately — no empty states on first open

### Build Variants

```bash
# Debug build (default)
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install directly to connected device
./gradlew installDebug
```

---

## 🗃️ Database Schema

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│   transactions   │       │    categories    │       │     goals        │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │       │ id (PK)          │
│ amount           │──────▶│ name             │◀──────│ categoryId (FK)  │
│ type (enum)      │       │ icon             │       │ budgetLimit      │
│ categoryId (FK)  │       │ colorHex         │       │ month            │
│ title            │       │ isDefault        │       │ year             │
│ note             │       └──────────────────┘       │ createdAt        │
│ date (epoch ms)  │                                   └──────────────────┘
│ createdAt        │
└──────────────────┘

Constraints:
• transactions.categoryId → categories.id (ON DELETE SET DEFAULT)
• goals.categoryId → categories.id (ON DELETE CASCADE)
• One goal per categoryId + month + year (enforced in repository layer)
```

---

## 💡 Key Design Decisions

### 1. Room + Flow = Zero Manual Refresh
Every DAO query returns `Flow<T>`. When a transaction is added, the `Flow` emits a new value and every screen observing it (Home balance, Transaction list, Insights chart) updates automatically. No `LiveData`, no manual `notifyDataSetChanged()`, no polling.

### 2. Mock API Architecture Mirrors Production
The `MockApiInterceptor` is a drop-in OkHttp interceptor that returns hardcoded JSON with a 600ms delay. The Retrofit service interface, repositories, ViewModels, and UI have no knowledge of the mock — they consume `ApiResult<T>` the same way they would with a real API. One line change in `NetworkModule.kt` connects to production.

### 3. Biometric Uses AppCompatActivity
`BiometricPrompt` requires a `FragmentActivity`. `MainActivity` extends `AppCompatActivity` (which extends `FragmentActivity`) rather than `ComponentActivity`, enabling native biometric integration without workarounds.

### 4. DataStore Over SharedPreferences
All user preferences (name, currency, dark mode, biometric toggle, reminder settings) are stored in `DataStore<Preferences>`. It's type-safe, coroutine-native, and handles concurrent reads without ANR risk — unlike `SharedPreferences`.

### 5. Goal Feature: Monthly Budget Per Category
Chosen because it maps directly to real behaviour — people naturally think in monthly budgets per category ("I want to spend max ₹5000 on food this month"). It integrates naturally with transaction data, enables the most actionable insight ("you're 80% through your food budget with 12 days left"), and produces the alert system that differentiates the app.

### 6. Financial Health Score
A composite 0–100 score across three dimensions: savings rate (0–30), budget adherence (0–40), and tracking consistency (0–30). Displayed as an animated arc ring on the Home screen. Gives users a single number to improve — far more actionable than a wall of statistics.

### 7. Swipe-to-Delete with Undo
Destructive actions must be reversible. Swipe-left-to-delete triggers immediately (for snappy UX) but shows a 5-second undo snackbar that restores the transaction. This follows Material Design's recommended pattern for list item deletion.

---

## 🔮 What Would Be Added With More Time

- **Recurring transactions** — auto-add salary, rent, subscriptions each month
- **Lottie animations** — animated illustrations for empty states and onboarding
- **Multi-account support** — separate wallets (cash, bank, credit card)
- **Photo receipts** — attach camera photos to transactions
- **Budget rollover** — unused budget from last month carries forward
- **Data charts export** — export insights as PDF report
- **Widget** — home screen widget showing current balance
- **Wear OS companion** — quick expense logging from the wrist
- **Real backend integration** — Spring Boot REST API + JWT auth
- **Google Drive backup** — automatic transaction backup to user's Drive

---

## 📋 Assignment Coverage

| Requirement | Status | Notes |
|---|---|---|
| Home Dashboard | ✅ Complete | Balance, income, expense, chart, savings rate |
| Transaction Tracking | ✅ Complete | CRUD, filter, search, swipe-delete, undo |
| Goal / Challenge Feature | ✅ Complete | Monthly budget per category with alerts + edit |
| Insights Screen | ✅ Complete | 7-day chart, week/month comparison, AI tips |
| Mobile UX | ✅ Complete | Empty states, shimmer, transitions, validation |
| Local Data Handling | ✅ Complete | Room DB + Flow + DataStore |
| Code Structure | ✅ Complete | MVVM + Repository + Hilt DI |
| Dark Mode | ✅ Complete | Full dark theme via DataStore |
| Notifications | ✅ Complete | WorkManager daily reminder + budget alerts |
| Animated Transitions | ✅ Complete | Slide + fade between all screens |
| Biometric Lock | ✅ Complete | Fingerprint / PIN on app launch |
| Data Export | ✅ Complete | CSV export via FileProvider + share intent |
| Profile Settings | ✅ Complete | Name, currency, dark mode, notifications |
| Offline-First | ✅ Complete | All core features work without internet |
| Multi-currency | ✅ Complete | 8 currencies with live rates from mock API |
| Mock API Integration | ✅ Bonus | Retrofit + OkHttp interceptor (not required) |
| Financial Health Score | ✅ Bonus | Animated composite score (not required) |
| Month-End Forecast | ✅ Bonus | Linear spending projection (not required) |

---

## 👨‍💻 Author

**Nitish Choubey**  
📧 [your.email@example.com]  
🔗 [github.com/yourusername]  
💼 [linkedin.com/in/yourusername]

---

## 📄 License

```
MIT License

Copyright (c) 2026 Nitish Choubey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<div align="center">

**Built with ❤️ using Kotlin + Jetpack Compose**

*Submitted for Zorvyn Mobile Developer Intern Screening — April 2026*

</div>
