# FINO – Expense Manager (Android)

Android expense tracker built with Kotlin, Jetpack Compose and Firebase.

## Features (current)
- Email/password sign-up, login, password reset (Firebase Auth)
- Add income and expenses; dashboard with totals and category pie chart
- Category spending limits with notifications
- Savings goals and scheduled-transaction reminders
- CSV export and a text report
- "AI budget" (on-device TFLite) and a Gemini chat assistant (Firebase AI Logic)

## Known limitations
- No edit/delete for transactions yet
- Scheduled transactions only send a reminder
- No automated tests or CI yet
- Receipt images are not uploaded

## Tech stack
Kotlin · Jetpack Compose · XML/ViewBinding (auth screens) · Firebase Auth,
Firestore, AI Logic, App Check · WorkManager · TensorFlow Lite · MPAndroidChart

## Setup
1. Install Android Studio and JDK 17.
2. Create a Firebase project and add an Android app with package
   `com.example.expensemanager`.
3. Download `google-services.json` and place it in `app/`
   (it is gitignored – never commit it).
4. In the Firebase console enable **Authentication → Email/Password** and
   create a **Firestore** database.
5. Deploy the rules: `firebase deploy --only firestore:rules`.
6. Enable **Firebase AI Logic** (Gemini Developer API) for the chat feature.
7. Debug builds use the App Check debug provider: run the app once, copy the
   debug token from Logcat, and register it under App Check → Apps.
8. Build and run the `app` module.

## Data model
`users/{uid}` → subcollections `transactions`, `totals/totalsDoc`, `limits`,
`goals`, `scheduled_transactions`.

## Security
Firestore rules restrict each user to their own data (`firestore.rules`).
No secrets are committed; see `.gitignore`.