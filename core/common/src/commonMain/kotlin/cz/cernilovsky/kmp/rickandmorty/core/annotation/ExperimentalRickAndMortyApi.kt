package cz.cernilovsky.kmp.rickandmorty.core.annotation

/** Marks API that may change in any release, including patch releases. */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This Rick and Morty API is experimental and may change without a major version bump.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
public annotation class ExperimentalRickAndMortyApi
