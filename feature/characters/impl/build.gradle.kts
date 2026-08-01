plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.kmp.published")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.characters.api)
            implementation(projects.feature.episode.api)
            implementation(projects.feature.location.api)
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.database)
            implementation(projects.core.featureflags)
            implementation(libs.androidx.paging.common)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
