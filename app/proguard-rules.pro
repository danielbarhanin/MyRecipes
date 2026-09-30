# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep attributes required for Firebase reflection
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Keep Firebase Realtime Database GenericTypeIndicator and its subclasses
-keep class com.google.firebase.database.GenericTypeIndicator
-keep class * extends com.google.firebase.database.GenericTypeIndicator { *; }
-keepclassmembers class * extends com.google.firebase.database.GenericTypeIndicator { *; }

# Keep Firebase Realtime Database and Auth data model classes
-keep class com.daniel.myrecipes.app.data.** { *; }
-keepclassmembers class com.daniel.myrecipes.app.data.** { *; }

# Keep Enum values and valueOf methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable CREATOR fields
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

