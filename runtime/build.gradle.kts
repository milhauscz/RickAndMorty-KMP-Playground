import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("rickandmorty.kmp.feature")
    id("rickandmorty.kmp.published")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.nativecoroutines)
}

val frameworkName = "RickAndMortySDKCore"

kotlin {
    compilerOptions {
        optIn.add("kotlin.experimental.ExperimentalObjCName")
        optIn.add("kotlin.experimental.ExperimentalObjCRefinement")
    }

    val xcframework = XCFramework(frameworkName)
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
            isStatic = true
            export(libs.kotlinx.coroutines.core)
            export(projects.core.common)
            export(projects.feature.characters.api)
            export(projects.feature.characters.impl)
            xcframework.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.feature.characters.api)
            api(projects.feature.characters.impl)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.featureflags)
            implementation(projects.core.designsystem)
            implementation(projects.core.image)
            implementation(projects.feature.episode.impl)
            implementation(projects.feature.location.impl)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.paging.common)
            implementation(libs.coil.compose)
            // Must be on commonMain so KSP sees @NativeCoroutinesRefined on CharactersIosBridge
            // (iosMain-only deps are invisible to the common KSP round).
            implementation(libs.kmp.nativecoroutines.annotations)
            implementation(libs.kmp.nativecoroutines.core)
        }
    }
}

dependencies {
    add("androidHostTestImplementation", libs.robolectric)
    add("androidHostTestImplementation", libs.androidx.testExt.junit)
}
