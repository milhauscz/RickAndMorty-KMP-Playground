plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.kmp.published")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Result / DataError / Episode appear in EpisodeRepository's public API.
            api(projects.core.common)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
