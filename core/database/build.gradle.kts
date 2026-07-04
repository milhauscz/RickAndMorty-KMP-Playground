plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DAO signatures expose PagingSource to consuming feature modules.
            api(libs.androidx.paging.common)
            // BuildConfig.isDebug drives allowDestructiveMigration.
            implementation(projects.core.common)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
