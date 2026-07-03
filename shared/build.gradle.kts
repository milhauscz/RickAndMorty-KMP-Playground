plugins {
    id("rickandmorty.kmp.feature")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core modules whose Koin modules are aggregated in initKoin / used by App.
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.designsystem)
            implementation(projects.core.image)
            implementation(projects.feature.episode)
            implementation(projects.feature.location)
            implementation(projects.feature.characters)
            // Coil singleton factory + NavHost + type-safe serializable routes.
            implementation(libs.coil.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
