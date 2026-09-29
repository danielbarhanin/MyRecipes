# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep attributes required for Firebase/Gson reflection
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Keep Firebase Realtime Database and Auth data model classes
-keep class com.daniel.myrecipes.app.data.** { *; }
-keepclassmembers class com.daniel.myrecipes.app.data.** { *; }
