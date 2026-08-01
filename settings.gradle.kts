rootProject.name = "RickandMorty"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
include(":runtime")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:designsystem")
include(":core:featureflags")
include(":core:image")
include(":feature:episode:api", ":feature:episode:impl")
include(":feature:location:api", ":feature:location:impl")
include(":feature:characters:api", ":feature:characters:impl", ":feature:characters:ui")
include(":konsist")
