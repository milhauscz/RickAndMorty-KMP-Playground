plugins {
    // Declared so the `kotlin { androidLibrary { } }` accessors are generated for this
    // precompiled script. Applied idempotently alongside `rickandmorty.kmp.library`.
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val catalog = libs
val resPackage = androidNamespace + ".resources"

compose.resources {
    publicResClass = true
    packageOfResClass = resPackage
}

kotlin {
    androidLibrary {
        androidResources {
            enable = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(catalog.findLibrary("compose-runtime").get())
            implementation(catalog.findLibrary("compose-foundation").get())
            implementation(catalog.findLibrary("compose-material3").get())
            implementation(catalog.findLibrary("compose-ui").get())
            implementation(catalog.findLibrary("compose-components-resources").get())
            implementation(catalog.findLibrary("compose-uiToolingPreview").get())
        }
        androidMain.dependencies {
            implementation(catalog.findLibrary("compose-uiToolingPreview").get())
        }
    }
}
