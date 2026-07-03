plugins {
    // Declared so the `kotlin { sourceSets { } }` accessors are generated for this
    // precompiled script. Applied idempotently alongside `rickandmorty.kmp.library`.
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

val catalog = libs

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(catalog.findLibrary("androidx-room-runtime").get())
            implementation(catalog.findLibrary("androidx-room-paging").get())
            implementation(catalog.findLibrary("androidx-sqlite-bundled").get())
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", catalog.findLibrary("androidx-room-compiler").get())
    add("kspIosArm64", catalog.findLibrary("androidx-room-compiler").get())
    add("kspIosSimulatorArm64", catalog.findLibrary("androidx-room-compiler").get())
}
