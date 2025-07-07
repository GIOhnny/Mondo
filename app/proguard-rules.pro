# Generic ProGuard rules for an Android application.
# You WILL LIKELY NEED TO ADD MORE RULES specific to your app and libraries.
-keep class ro.giohnnysoftware.mondo.*
# Keep the no-argument constructor for UserPojo
-keep class ro.giohnnysoftware.mondo.* { public <init>(); }
-keep class ro.giohnnysoftware.mondo.library.* { public <init>(); }
# Keep application, activities, services, receivers, providers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Keep custom views and their constructors (if used in XML layouts)
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelable implementations and their CREATOR field
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep classes annotated with @Keep
# Add @androidx.annotation.Keep to classes/methods/fields you want to preserve
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# Keep native method names and the classes that contain them
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes that are accessed via valueOf().
# -keepnames class com.example.MyEnum # Example: Uncomment and replace with your enum

# For libraries like Gson, Retrofit, Firebase, etc., they often bundle their own
# consumer ProGuard rules. However, if you have issues, you might need to add
# rules specific to them. Always check the library's official documentation.

# Example for a data class used with Firebase or Gson (common pattern):
# -keep public class ro.giohnnysoftware.mondo.models.YourDataModel {
#     public <init>(); # Keep no-argument constructor
#     public *;       # Keep all public fields and methods (or be more specific)
# }

# === ADD YOUR CUSTOM RULES BELOW BASED ON CRASHES AND WARNINGS ===
# When your app crashes with minifyEnabled=true, check Logcat.
# The error message (e.g., ClassNotFoundException, NoSuchMethodError)
# will tell you what class/method/field was removed or renamed.
# Use the mapping.txt file (app/build/outputs/mapping/release/mapping.txt)
# to de-obfuscate names if needed.

# Example: If Logcat says "ClassNotFoundException: com.example.MyImportantClass"
# -keep class com.example.MyImportantClass

# Example: If Logcat says "NoSuchMethodError: com.example.MyImportantClass.doSomething"
# -keep class com.example.MyImportantClass {
# public void doSomething(...); # Make sure the signature matches
# }

