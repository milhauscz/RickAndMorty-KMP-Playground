plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.kmp.published")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Result / DataError / Location appear in LocationRepository's public API.
            api(projects.core.common)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
