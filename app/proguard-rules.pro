# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in the
# Android SDK's default proguard-android-optimize.txt.
#
# For more details, see:
#   https://www.guardsquare.com/manual/configuration/usage

# ═══════════════════════════════════════════════════════════════════════════
# GENERAL KOTLIN
# ═══════════════════════════════════════════════════════════════════════════

# Keep Kotlin Metadata so reflection-based code (Gson, Moshi, etc.) works.
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses,EnclosingMethod

# Kotlin serialization
-keepattributes RuntimeVisibleAnnotations
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# ═══════════════════════════════════════════════════════════════════════════
# KOTLIN COROUTINES
# ═══════════════════════════════════════════════════════════════════════════

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ═══════════════════════════════════════════════════════════════════════════
# FIREBASE ANALYTICS
# ═══════════════════════════════════════════════════════════════════════════

-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.firebase.analytics.**

# ═══════════════════════════════════════════════════════════════════════════
# FIREBASE CRASHLYTICS
# ═══════════════════════════════════════════════════════════════════════════

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep custom exception classes for clear crash reports
-keep public class * extends java.lang.Exception

-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ═══════════════════════════════════════════════════════════════════════════
# GOOGLE ADMOB / PLAY SERVICES ADS
# ═══════════════════════════════════════════════════════════════════════════

-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Mediation adapter base classes
-keep class com.google.android.gms.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.mediation.**

# ═══════════════════════════════════════════════════════════════════════════
# GOOGLE PLAY SERVICES (general)
# ═══════════════════════════════════════════════════════════════════════════

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ═══════════════════════════════════════════════════════════════════════════
# ROOM DATABASE
# ═══════════════════════════════════════════════════════════════════════════

# Keep all Room entity, DAO, and database classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.**

# ═══════════════════════════════════════════════════════════════════════════
# HILT (Dependency Injection)
# ═══════════════════════════════════════════════════════════════════════════

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }
-keepclasseswithmembernames class * {
    @dagger.* <methods>;
    @javax.inject.* <methods>;
    @dagger.hilt.* <methods>;
}
-dontwarn dagger.**
-dontwarn hilt_aggregated_deps.**

# ═══════════════════════════════════════════════════════════════════════════
# ZXING QR CODE LIBRARY
# ═══════════════════════════════════════════════════════════════════════════

-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ═══════════════════════════════════════════════════════════════════════════
# ITEXT PDF LIBRARY
# ═══════════════════════════════════════════════════════════════════════════

-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-keepclassmembers class com.itextpdf.** { *; }

# Bouncycastle (used by iText for digital signatures)
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ═══════════════════════════════════════════════════════════════════════════
# COIL IMAGE LOADING
# ═══════════════════════════════════════════════════════════════════════════

-keep class coil.** { *; }
-dontwarn coil.**

# OkHttp (used by Coil under the hood)
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ═══════════════════════════════════════════════════════════════════════════
# BILLCRAFT MODEL CLASSES
# Keep all data/model/entity classes to prevent field name obfuscation which
# would break Room column mapping and serialization.
# ═══════════════════════════════════════════════════════════════════════════

-keep class com.billcraft.app.data.model.** { *; }
-keep class com.billcraft.app.data.entity.** { *; }
-keep class com.billcraft.app.data.db.entity.** { *; }
-keep class com.billcraft.app.domain.model.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# JETPACK COMPOSE
# ═══════════════════════════════════════════════════════════════════════════

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ═══════════════════════════════════════════════════════════════════════════
# NAVIGATION COMPONENT
# ═══════════════════════════════════════════════════════════════════════════

-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ═══════════════════════════════════════════════════════════════════════════
# VIEWMODEL / LIFECYCLE
# ═══════════════════════════════════════════════════════════════════════════

-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ═══════════════════════════════════════════════════════════════════════════
# ENUMS (always keep — obfuscation can break enum name() calls)
# ═══════════════════════════════════════════════════════════════════════════

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ═══════════════════════════════════════════════════════════════════════════
# PARCELABLE
# ═══════════════════════════════════════════════════════════════════════════

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ═══════════════════════════════════════════════════════════════════════════
# SERIALIZABLE
# ═══════════════════════════════════════════════════════════════════════════

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

# ═══════════════════════════════════════════════════════════════════════════
# MISC / SUPPRESS WARNINGS
# ═══════════════════════════════════════════════════════════════════════════

-dontwarn sun.misc.**
-dontwarn java.lang.invoke.**
-dontwarn org.slf4j.**
-dontwarn org.apache.**
