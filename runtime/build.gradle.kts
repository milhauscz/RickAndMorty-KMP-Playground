import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("rickandmorty.kmp.feature")
    id("rickandmorty.kmp.published")
}

val frameworkName = "RickAndMortySDK"

kotlin {
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

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        filters {
            exclude {
                annotatedWith.add("cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi")
                annotatedWith.add("cz.cernilovsky.kmp.rickandmorty.runtime.InternalRickAndMortyRuntimeApi")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.featureflags)
            implementation(projects.core.designsystem)
            implementation(projects.core.image)
            implementation(projects.feature.characters.api)
            implementation(projects.feature.characters.impl)
            implementation(projects.feature.episode.api)
            implementation(projects.feature.episode.impl)
            implementation(projects.feature.location.api)
            implementation(projects.feature.location.impl)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.paging.common)
            implementation(libs.coil.compose)
        }
    }
}

dependencies {
    add("androidHostTestImplementation", libs.robolectric)
    add("androidHostTestImplementation", libs.androidx.testExt.junit)
}
