# Project specific ProGuard / R8 rules

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

# Keep Apache POI models and XmlBeans reflection
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class openxmlformats.** { *; }

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
