# Divine Canvas — R8 / ProGuard rules
# General optimization is handled by proguard-android-optimize.txt.

# --- Kotlin metadata ---
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontwarn kotlin.**

# --- kotlinx.serialization ---
# Keep @Serializable classes' generated serializers.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers,allowshrinking class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.divinecanvas.**$$serializer { *; }
-keepclassmembers class com.divinecanvas.** {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# --- Retrofit / OkHttp ---
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**

# --- Room (entities accessed reflectively at schema gen are kept by KSP) ---
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# --- Hilt / Dagger ---
-dontwarn com.google.errorprone.annotations.**

# --- Models: keep DTO + entity field names for serialization mapping ---
-keep class com.divinecanvas.data.remote.dto.** { *; }
-keep class com.divinecanvas.data.local.entity.** { *; }

# --- Coil ---
-dontwarn coil.**

# --- Credential Manager / Google Identity ---
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.gms.**

# Keep enum values used in saved/serialized state.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
