plugins {
    id("rickandmorty.kmp.feature")
    id("rickandmorty.kmp.published")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.characters.api)
            api(projects.feature.characters.impl)
            implementation(projects.core.designsystem)
            implementation(projects.core.image)
            implementation(libs.coil.compose)
            implementation(libs.androidx.paging.compose)
            implementation(libs.androidx.window.core)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.layout)
            implementation(libs.compose.adaptive.navigation)
            implementation(libs.compose.ui.backhandler)
        }
        commonTest.dependencies {
            implementation(projects.core.featureflags)
            implementation(libs.androidx.paging.common)
        }
    }
}
