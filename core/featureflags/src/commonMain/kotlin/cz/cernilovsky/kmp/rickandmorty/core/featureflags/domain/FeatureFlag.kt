package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * A behaviour that can be turned on without shipping a new build.
 *
 * The key is the contract with whoever edits the remote config or writes an override, so it is a
 * string chosen once and never renamed — renaming it silently reverts every environment to
 * [defaultEnabled], which is the kind of change that looks harmless in review.
 *
 * [defaultEnabled] is what the code does when nothing else has an opinion: no remote config yet, no
 * network, or a flag the backend has never heard of. It should describe the behaviour the release
 * was tested with, which for new work means `false`.
 */
public data class FeatureFlag(
    val key: String,
    val defaultEnabled: Boolean,
)
