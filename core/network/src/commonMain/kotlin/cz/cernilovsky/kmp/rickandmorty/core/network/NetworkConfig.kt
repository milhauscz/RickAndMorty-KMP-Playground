package cz.cernilovsky.kmp.rickandmorty.core.network

/**
 * Network settings for the SDK HTTP client.
 *
 * Pass a custom [baseUrl] to target staging or a mock server.
 */
data class NetworkConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    /** Null follows the build type's debug flag; a non-null value overrides it. */
    val loggingEnabled: Boolean? = null,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://rickandmortyapi.com/api"
    }
}
