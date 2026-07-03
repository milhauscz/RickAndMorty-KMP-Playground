plugins {
    id("rickandmorty.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Result / DataError / Episode appear in IEpisodeRepository's public API.
            api(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
