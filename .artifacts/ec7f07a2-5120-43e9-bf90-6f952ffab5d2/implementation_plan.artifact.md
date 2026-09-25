# Fix WorkManager Crash in Release Build

The app is crashing in the release build with a `NoSuchMethodException` for `androidx.work.impl.WorkDatabase_Impl`. This is caused by R8 (ProGuard) stripping away the constructor of the Room-generated implementation class used by WorkManager.

## Proposed Changes

### [Component Name] ProGuard Configuration

#### [MODIFY] [proguard-rules.pro](file:///C:/Users/User/CreativeIT/HealthBell2/app/proguard-rules.pro)
Add rules to keep WorkManager and Room implementation classes to prevent them from being stripped or obfuscated in a way that breaks reflection.

## Verification Plan

### Manual Verification
- Build the app in release mode: `./gradlew assembleRelease`
- Install the release APK on a device or emulator.
- Verify that the app starts without crashing at the splash screen.
