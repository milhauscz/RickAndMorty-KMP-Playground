plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
    // Konsist scans all modules; avoid stale skips when only other modules changed.
    outputs.upToDateWhen { false }
}
