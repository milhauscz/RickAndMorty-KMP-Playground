plugins {
    id("rickandmorty.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // BuildConfig is provided through Koin (commonPlatformModule).
            implementation(libs.koin.core)
        }
    }
}
