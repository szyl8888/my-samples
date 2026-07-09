# Call and SMS Blocker (Android sample)

This is a sample Android project (Kotlin) that demonstrates:

- Call screening via CallScreeningService (reject/skip call log/notification)
- SMS interception as a default SMS app (filtering based on prefix/number)
- Integration with libphonenumber for number normalization
- Initial blacklist of common Chinese mobile prefixes

Branch: call-sms-blocker-sample

Build:
- Open in Android Studio (Gradle plugin 8.1.0, Kotlin 1.9.0)
- Compile SDK 34, minSdk 24

Notes:
- You must set the app as the default SMS app for SMS interception to work.
- You must enable the app as a call screening app in system settings for call blocking to work.
- Use the UI to manage prefixes (comma-separated). Default prefixes include common China mobile segments and 010.

Limitations:
- Simple UI and storage (SharedPreferences). Not production-ready.
- Test carefully to avoid blocking important numbers (bank, verification codes). Use whitelist if needed.
