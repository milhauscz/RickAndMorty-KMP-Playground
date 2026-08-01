package cz.cernilovsky.kmp.rickandmorty.runtime

/**
 * Host-supplied configuration for [RickAndMortySdk].
 */
public class RickAndMortySdkConfig private constructor(
    /** Root of the Rick and Morty HTTP API, without a trailing slash. */
    public val baseUrl: String,
    /** When true, emits verbose HTTP logs. */
    public val loggingEnabled: Boolean,
    /** Remote feature-flag document URL, or null to use defaults only. */
    public val remoteConfigUrl: String?,
    /** Feature-flag overrides keyed by [RickAndMortyFeatureFlags] constants. */
    public val featureFlagOverrides: Map<String, Boolean>,
) {
    public class Builder {
        private var baseUrl: String = DEFAULT_BASE_URL
        private var loggingEnabled: Boolean = false
        private var remoteConfigUrl: String? = null
        private val featureFlagOverrides: MutableMap<String, Boolean> = mutableMapOf()

        public fun baseUrl(value: String): Builder =
            apply {
                baseUrl = value.trimEnd('/')
            }

        public fun loggingEnabled(value: Boolean): Builder = apply { loggingEnabled = value }

        public fun remoteConfigUrl(value: String): Builder = apply { remoteConfigUrl = value }

        public fun overrideFeatureFlag(
            key: String,
            enabled: Boolean,
        ): Builder = apply { featureFlagOverrides[key] = enabled }

        public fun build(): RickAndMortySdkConfig =
            RickAndMortySdkConfig(
                baseUrl = baseUrl,
                loggingEnabled = loggingEnabled,
                remoteConfigUrl = remoteConfigUrl,
                featureFlagOverrides = featureFlagOverrides.toMap(),
            )
    }

    public companion object {
        public const val DEFAULT_BASE_URL: String = "https://rickandmortyapi.com/api"

        public fun builder(): Builder = Builder()

        public fun default(): RickAndMortySdkConfig = Builder().build()
    }
}
