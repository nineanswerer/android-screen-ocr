# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep data classes
-keep class com.screentextocr.data.model.** { *; }

# Keep domain models
-keep class com.screentextocr.domain.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
