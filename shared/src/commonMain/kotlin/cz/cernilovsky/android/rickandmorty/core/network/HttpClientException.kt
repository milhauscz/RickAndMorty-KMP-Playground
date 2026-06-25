package cz.cernilovsky.android.rickandmorty.core.network

import cz.cernilovsky.android.rickandmorty.core.domain.DataError

class HttpClientException(val error: DataError.Remote) : Exception() {
}