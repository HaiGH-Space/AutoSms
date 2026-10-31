# Project specific ProGuard / R8 rules

# Keep Attributes for Reflection & Annotations required by Apache POI, Log4j & Room
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
-keep class org.apache.poi.** { *; }
-keepclassmembers class org.apache.poi.** { *; }

-keep class org.apache.xmlbeans.** { *; }
-keepclassmembers class org.apache.xmlbeans.** { *; }

-keep class org.openxmlformats.** { *; }
-keepclassmembers class org.openxmlformats.** { *; }

-keep class org.etsi.uri.** { *; }
-keepclassmembers class org.etsi.uri.** { *; }

-keep class com.microsoft.schemas.** { *; }
-keepclassmembers class com.microsoft.schemas.** { *; }

-keep class org.apache.commons.** { *; }
-keepclassmembers class org.apache.commons.** { *; }

-keep class org.apache.logging.log4j.** { *; }
-keepclassmembers class org.apache.logging.log4j.** { *; }

# Room database entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Keep Domain Data Models
-keep class com.gmail.nthai.domain.model.** { *; }
-keep class com.gmail.nthai.data.local.entity.** { *; }

# Keep Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
