# ============================================================================
# ProGuard/R8 Rules for NFC Manager
# Updated: 2025-10-16
# ============================================================================
# This file contains comprehensive ProGuard rules for:
# - Room Database
# - Hilt/Dagger
# - Kotlin Coroutines
# - Jetpack Compose
# - NFC APIs
# - Security & Obfuscation
# ============================================================================

# ============================================================================
# General Android Rules
# ============================================================================

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ============================================================================
# Kotlin Rules
# ============================================================================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Kotlin serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# ============================================================================
# Room Database Rules
# ============================================================================

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room DAOs
-keep interface * extends androidx.room.Dao { *; }

# Keep Room entities
-keep @androidx.room.Entity class * { *; }

# Keep database classes
-keep class com.dxbmark.nfcmanager.data.database.** { *; }

# ============================================================================
# Hilt/Dagger Rules
# ============================================================================

# Keep Hilt generated classes
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt modules
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.components.SingletonComponent class * { *; }

# Keep injected constructors
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep Hilt entry points
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ============================================================================
# Jetpack Compose Rules
# ============================================================================

# Keep Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class androidx.compose.** { *; }

# Keep Compose runtime
-keepclassmembers class androidx.compose.runtime.** { *; }

# Keep Compose UI
-keep class androidx.compose.ui.** { *; }

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# ============================================================================
# NFC Manager Specific Rules
# ============================================================================

# Keep all NFC Manager classes (can be optimized later)
-keep class com.dxbmark.nfcmanager.** { *; }

# Android NFC specific rules
-keep class android.nfc.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================================================
# Security Rules
# ============================================================================

# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove console logs
-assumenosideeffects class java.io.PrintStream {
     public void println(%);
     public void println(**);
}

# Remove our custom logger in release
-assumenosideeffects class com.dxbmark.nfcmanager.utils.error.AppLogger {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** log(...);
    public static *** nfc(...);
    public static *** database(...);
    public static *** network(...);
    public static *** service(...);
    public static *** ui(...);
    public static *** viewModel(...);
}

# ============================================================================
# Standard Android Rules
# ============================================================================

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================================================
# Optimization & Obfuscation
# ============================================================================

# Enable aggressive optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization options
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Print mapping for crash reports
-printmapping mapping.txt

# ============================================================================
# Warnings to Ignore
# ============================================================================

# Ignore warnings for missing classes
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ============================================================================
# End of ProGuard Rules
# ============================================================================
