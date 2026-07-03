import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("rickandmorty.lint")
}

val catalog = libs
val ns = androidNamespace
val compile = androidCompileSdk
val min = androidMinSdk

kotlin {
    androidLibrary {
        namespace = ns
        compileSdk = compile
        minSdk = min

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        // Creates the JVM host-test task so commonTest runs on Android host (not just iOS).
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(catalog.findLibrary("kotlin-test").get())
            implementation(catalog.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}

dependencies {
    // JUnit backs kotlin-test on the Android host-test runtime.
    add("androidHostTestImplementation", catalog.findLibrary("junit").get())
}
