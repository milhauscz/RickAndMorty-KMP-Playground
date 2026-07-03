plugins {
    id("rickandmorty.kmp.library")
    id("rickandmorty.compose")
}

val catalog = libs

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(catalog.findLibrary("koin-core").get())
            implementation(catalog.findLibrary("koin-compose").get())
            implementation(catalog.findLibrary("koin-compose-viewmodel").get())
            implementation(catalog.findLibrary("androidx-lifecycle-viewmodelCompose").get())
            implementation(catalog.findLibrary("androidx-lifecycle-runtimeCompose").get())
        }
        androidMain.dependencies {
            implementation(catalog.findLibrary("koin-android").get())
        }
    }
}

dependencies {
    add("androidHostTestImplementation", catalog.findLibrary("robolectric").get())
    add("androidHostTestImplementation", catalog.findLibrary("compose-ui-test-junit4").get())
    add("androidRuntimeClasspath", catalog.findLibrary("compose-uiTooling").get())
}
