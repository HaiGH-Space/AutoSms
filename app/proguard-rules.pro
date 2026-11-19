# Project specific ProGuard / R8 rules

# Keep Attributes for Reflection & Annotations required by Apache POI & Room
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Apache POI & dependencies optional class ignores
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn java.awt.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.apache.commons.compress.**
-dontwarn com.graphbuilder.curve.**
-dontwarn javax.xml.stream.**
-dontwarn com.sun.javadoc.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.poi.xslf.draw.SVGUserAgent

# Ignore missing optional schemas referenced in POI OOXML
-dontwarn com.microsoft.schemas.**
-dontwarn org.etsi.uri.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.w3.x2000.x09.xmldsig.**

# Keep Apache POI models, XmlBeans, OpenXMLFormats & dependencies reflection
# (-keep class with {*} already keeps all class members and methods)
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.etsi.uri.** { *; }
-keep class com.microsoft.schemas.** { *; }
-keep class org.apache.commons.** { *; }
-keep class org.apache.logging.log4j.** { *; }

# Room database entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Keep Domain Data Models & Entities (JSON serialization & Room)
-keep class com.gmail.nthai.domain.model.** { *; }
-keep class com.gmail.nthai.data.local.entity.** { *; }

# Strip verbose/debug logs in Release build for performance & security
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
