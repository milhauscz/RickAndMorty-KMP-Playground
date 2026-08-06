plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kmp.nativecoroutines) apply false
    // Applied here rather than per module: an SBOM answers "what is in the thing we shipped", and
    // that question is about the whole dependency graph, not one module's slice of it.
    alias(libs.plugins.cyclonedx)
}

// One aggregated document for the whole build, named and versioned after the thing that is actually
// released. An SBOM is only useful if a reader can tie it to an artifact they have.
tasks.cyclonedxBom {
    componentName = "rick-and-morty-sdk"
    componentVersion = providers.gradleProperty("VERSION_NAME")
    jsonOutput = layout.buildDirectory.file("reports/sbom/sbom.json").get().asFile
    // JSON only: it is what every scanner ingests, and a second copy of the same facts in XML is one
    // more file to keep in sync for no reader's benefit.
    xmlOutput.unsetConvention()
}

// Test and tooling dependencies are in nothing we ship. Leaving them in makes the document longer
// and every CVE triage slower, which is the opposite of what an SBOM is for. The filter applies to
// the per-project task only; the aggregate has no equivalent option and covers every configuration,
// which is why the CI job publishes `:runtime:cyclonedxDirectBom` rather than the aggregate.
allprojects {
    tasks.cyclonedxDirectBom {
        skipConfigs = listOf(".*[Tt]est.*", ".*[Ll]int.*", ".*etekt.*", ".*[Kk]sp.*")
    }
}

tasks.register("konsistCheck") {
    group = "verification"
    description = "Runs Konsist architecture checks"
    dependsOn(":konsist:test")
}

// These tasks are registered per module, not in the root project — the command line resolves an
// unqualified name across every project, but `dependsOn` does not, so they are collected explicitly.
// `matching` is a live view, so modules configured after this block are still picked up, and modules
// without ABI validation simply contribute nothing.
val perModuleQualityTasks = setOf("lintKotlin", "detekt", "checkKotlinAbi")

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs every static check the CI lint job runs: formatting, detekt, architecture and public API"
    dependsOn(subprojects.map { module -> module.tasks.matching { it.name in perModuleQualityTasks } })
    dependsOn(":konsist:test")
}