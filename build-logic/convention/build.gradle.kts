plugins {
    `kotlin-dsl`
}

group = "cz.cernilovsky.kmp.rickandmorty.buildlogic"

dependencies {
    // Gradle plugin artifacts on the classpath so precompiled script plugins
    // can apply them by id and use their DSLs.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlinxSerialization.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.composeCompiler.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.kotlinter.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
}
