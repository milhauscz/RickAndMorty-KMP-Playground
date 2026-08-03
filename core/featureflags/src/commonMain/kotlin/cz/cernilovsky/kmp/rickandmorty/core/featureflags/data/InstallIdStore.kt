package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

/**
 * Persisted install id for rollout bucketing. Survives process death so a "10% rollout" means
 *  10% of installations, not 10% of launches. Identifies an installation, not a person.
 */
internal interface InstallIdStore {
    fun installId(): String
}
