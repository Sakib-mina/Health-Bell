# Full-Screen Reminder System with FCM & Alarm Sound

This plan implements a highly reliable reminder system for Health Bell that triggers full-screen alerts even when the display is off (via FCM) or when other apps are in use.

## Proposed Changes

### Core Logic & Utilities

#### [NEW] [NotificationHelper](file:///C:/Users/User/CreativeIT/HealthBell2/app/src/main/java/com/ideacraftlab/healthbell/data/manager/NotificationHelper.kt)
Centralizes the logic for showing high-priority notifications with Full-Screen Intents. This ensures that when a reminder triggers (via local alarm or FCM), the UI pops up immediately.

#### [MODIFY] [AlarmReceiver](file:///C:/Users/User/CreativeIT/HealthBell2/app/src/main/java/com/ideacraftlab/healthbell/data/receiver/AlarmReceiver.kt)
Refactored to use `NotificationHelper`. Retains `WakeLock` logic to ensure the device wakes up for local scheduled alarms.

#### [NEW] [HealthBellMessagingService](file:///C:/Users/User/CreativeIT/HealthBell2/app/src/main/java/com/ideacraftlab/healthbell/data/service/HealthBellMessagingService.kt)
Handles incoming Firebase Cloud Messages (FCM). Specifically designed to trigger reminders when the display is off or from a remote server.

### Configuration & Infrastructure

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/User/CreativeIT/HealthBell2/app/src/main/AndroidManifest.xml)
- Registered `HealthBellMessagingService`.
- Added `INTERNET` and `ACCESS_NETWORK_STATE` permissions.
- Ensured `ReminderActivity` has the correct flags (`showOnLockScreen`, `turnScreenOn`) for lock-screen visibility.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/User/CreativeIT/HealthBell2/app/build.gradle.kts) & [libs.versions.toml](file:///C:/Users/User/CreativeIT/HealthBell2/gradle/libs.versions.toml)
Added `firebase-messaging` dependency.

#### [MODIFY] [FirebaseModule](file:///C:/Users/User/CreativeIT/HealthBell2/app/src/main/java/com/ideacraftlab/healthbell/core/di/FirebaseModule.kt)
Added Hilt provider for `FirebaseMessaging`.

## Reliability Measures ("1000% logic")
1.  **Full-Screen Intent**: Uses `setFullScreenIntent` which is the highest-priority mechanism in Android to show UI from background.
2.  **Notification Categories**: Categorized as `CATEGORY_ALARM` to bypass most system silencers.
3.  **Overlay Permission**: The app already requests `SYSTEM_ALERT_WINDOW` in `MainActivity`, which helps `ReminderActivity` appear over other apps.
4.  **Double Alarm Sound**: Sound is triggered both by the Notification Channel (system level) and `ReminderActivity` (app level) using `RingtoneManager`.
5.  **WakeLock**: Used in `AlarmReceiver` to ensure CPU doesn't sleep during notification processing.

## Verification Plan

### Manual Verification
1.  **Local Alarm**: Schedule a reminder (Water/Medicine) and lock the screen. Verify it wakes up and shows the full-screen UI with sound.
2.  **FCM Test**: Send a data message via Firebase Console:
    ```json
    {
      "to": "<TOKEN>",
      "data": {
        "type": "medicine",
        "med_name": "Paracetamol",
        "time": "08:00 AM"
      }
    }
    ```
    Verify it triggers the full-screen reminder even if the app is closed or display is off.
3.  **Foreground Test**: Use another app (e.g., Browser) and trigger a reminder. Verify the heads-up notification or immediate full-screen activity appears.
