package cz.cernilovsky.kmp.rickandmorty.core.network

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError

class HttpClientException(
    val error: DataError.Remote,
) : Exception() {
    override val message: String
        get() = error.name
}
