# Add project specific ProGuard rules here.
# Xposed modules should NOT be obfuscated
-keep class com.example.fakevpn.HookMain { *; }
-keep class de.robv.android.xposed.** { *; }
-keepattributes *Annotation*
