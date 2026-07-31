# Application code
-keep class cz.cernilovsky.kmp.rickandmorty.** { *; }

# Kotlin serialization (Ktor JSON, Room type converters, navigation routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class cz.cernilovsky.kmp.rickandmorty.**$$serializer { *; }
-keepclassmembers class cz.cernilovsky.kmp.rickandmorty.** {
    *** Companion;
}
-keepclasseswithmembers class cz.cernilovsky.kmp.rickandmorty.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**

# Koin
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module
-keepclassmembers class * { @org.koin.core.annotation.* <methods>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coil
-keep class coil3.** { *; }
-dontwarn coil3.**

# Compose (runtime retains @Composable metadata; keep line numbers for crash reports)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
