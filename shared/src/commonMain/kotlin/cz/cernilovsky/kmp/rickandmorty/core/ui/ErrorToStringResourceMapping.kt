package cz.cernilovsky.kmp.rickandmorty.core.ui

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientException
import org.jetbrains.compose.resources.StringResource
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.error_no_internet
import rickandmorty.shared.generated.resources.error_serialization
import rickandmorty.shared.generated.resources.error_server
import rickandmorty.shared.generated.resources.error_timeout
import rickandmorty.shared.generated.resources.error_too_many_requests
import rickandmorty.shared.generated.resources.error_unknown

fun Throwable.toMessageRes(): StringResource =
    when (this) {
        is HttpClientException -> error.toMessageRes()
        else -> Res.string.error_unknown
    }

fun DataError.Remote.toMessageRes(): StringResource =
    when (this) {
        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
        DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_timeout
        DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        DataError.Remote.SERVER -> Res.string.error_server
        DataError.Remote.SERIALIZATION -> Res.string.error_serialization
        DataError.Remote.UNKNOWN -> Res.string.error_unknown
    }
