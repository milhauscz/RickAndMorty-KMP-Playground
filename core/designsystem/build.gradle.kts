plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DataError appears in toMessageRes()'s public signature.
            api(projects.core.common)
            // HttpClientException is matched in Throwable.toMessageRes().
            implementation(projects.core.network)
        }
    }
}
