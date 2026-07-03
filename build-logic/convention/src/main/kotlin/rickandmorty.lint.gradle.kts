import io.gitlab.arturbosch.detekt.Detekt
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("org.jmailen.kotlinter")
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
    )
    buildUponDefaultConfig = true
}

tasks.withType<LintTask> {
    exclude { it.file.path.contains("${File.separator}build${File.separator}") }
}
tasks.withType<FormatTask> {
    exclude { it.file.path.contains("${File.separator}build${File.separator}") }
}
// The type-resolution detekt tasks (e.g. detektMetadataCommonMain) source every dir of their
// Kotlin compilation, which includes generated code such as the Compose resources `Res.kt`.
// Exclude generated `build/` output so detekt only analyses hand-written source, mirroring the
// kotlinter tasks above.
tasks.withType<Detekt>().configureEach {
    exclude { it.file.path.contains("${File.separator}build${File.separator}") }
}
