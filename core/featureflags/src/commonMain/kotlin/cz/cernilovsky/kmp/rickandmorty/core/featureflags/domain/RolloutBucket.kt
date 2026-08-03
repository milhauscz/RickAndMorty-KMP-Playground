package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

private const val FNV_OFFSET_BASIS = 2166136261u
private const val FNV_PRIME = 16777619u
private const val BUCKET_COUNT = 100u

/** Stable 0–99 bucket for an installation and flag key (percentage rollouts).
 * Included when rolloutBucket(installId, flagKey) < rolloutPercent.
 * Stable across launches/platforms; independent per flag (FNV-1a of installId:flagKey). */
internal fun rolloutBucket(
    installId: String,
    flagKey: String,
): Int {
    var hash = FNV_OFFSET_BASIS
    for (character in "$installId:$flagKey") {
        hash = hash xor character.code.toUInt()
        hash *= FNV_PRIME
    }
    return (hash % BUCKET_COUNT).toInt()
}
