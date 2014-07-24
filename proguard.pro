# override default number of optimization passes
-optimizationpasses 10


# Strip built-in logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
}


# Strip out SLF4J logging
-assumenosideeffects class org.slf4j.Logger {
    *** debug(...);
    *** info(...);
    *** trace(...);
    *** warn(...);
}


# newrelic settings
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses


# Ignore guava/jsr305 warnings
-dontwarn javax.annotation.*
-dontwarn javax.annotation.concurrent.*
-dontwarn sun.misc.Unsafe


# Google Play Services - Google Analytics
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
