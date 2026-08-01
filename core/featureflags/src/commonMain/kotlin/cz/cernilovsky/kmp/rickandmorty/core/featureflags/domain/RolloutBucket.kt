package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

private const val FNV_OFFSET_BASIS = 2166136261u
private const val FNV_PRIME = 16777619u
private const val BUCKET_COUNT = 100u

/**
 * Places an installation in one of 100 buckets for a given flag.
 *
 * Three properties matter, and each rules out an easier implementation:
 *
 * - **Stable across launches**, so an installation does not drift in and out of a rollout. That is
 *   why it hashes a persisted install id rather than calling a random number generator.
 * - **Stable across platforms**, so Android and iOS agree on who is in the group. `String.hashCode`
 *   would not do: its result is only specified on the JVM. FNV-1a is defined by its constants and
 *   produces the same number everywhere.
 * - **Independent per flag**, so a 10% rollout of one flag does not hand the same tenth of the user
 *   base every new behaviour. Mixing the flag key into the hash decorrelates the buckets.
 */
public fun rolloutBucket(
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
