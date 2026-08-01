package cz.cernilovsky.kmp.rickandmorty.core.network

/**
 * Network settings supplied by whoever owns the object graph.
 *
 * Was previously a hardcoded `const`, which is fine for an app that only ever talks to one backend
 * but not for a library: SDK consumers need to point it at a staging environment or a mock server,
 * and they cannot recompile us to do it.
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
