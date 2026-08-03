plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.kmp.published")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            api(libs.kotlinx.coroutines.core)
        }
    }
}
