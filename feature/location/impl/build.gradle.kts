plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.kmp.published")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.location.api)
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
