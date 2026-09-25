# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\User\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Hilt/Dagger rules
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.components.SingletonComponent class *

# Firebase rules
-keep class com.google.firebase.** { *; }

# Google Play Billing rules
-keep class com.android.billingclient.** { *; }

# Compose rules
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    *** onDraw(...);
}

# General optimization
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# WorkManager
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.background.systemalarm.RescheduleReceiver { *; }
-keep class androidx.work.impl.background.systemalarm.ConstraintProxy$* { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.foreground.SystemForegroundService { *; }
-dontwarn androidx.work.impl.WorkDatabase_Impl

# Keep domain models for Firestore/Data mapping
-keep class com.ideacraftlab.healthbell.domain.model.** { *; }

# Keep data classes to prevent field name obfuscation
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName *;
}

# Kotlin Coroutines and Reflection
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.ServiceLoaderLite { *; }
-dontwarn kotlinx.coroutines.**
