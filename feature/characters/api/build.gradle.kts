plugins {
    id("rickandmorty.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.feature.episode.api)
            api(projects.feature.location.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.paging.common)
        }
    }
}
