package cz.cernilovsky.kmp.rickandmorty.core.network

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend inline fun <reified T> safeCall(execute: suspend () -> HttpResponse): Result<T, DataError.Remote> {
    val response =
        try {
            execute()
        } catch (_: SocketTimeoutException) {
            return Result.Error(DataError.Remote.REQUEST_TIMEOUT)
        } catch (_: UnresolvedAddressException) {
            return Result.Error(DataError.Remote.NO_INTERNET)
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            return Result.Error(DataError.Remote.UNKNOWN)
        }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Remote> =
    when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (_: NoTransformationFoundException) {
                // Server sent a body we have no converter for (e.g. wrong content type).
                Result.Error(DataError.Remote.SERIALIZATION)
            } catch (_: ContentConvertException) {
                // Body was present but could not be deserialized (malformed / unexpected JSON).
                Result.Error(DataError.Remote.SERIALIZATION)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                Result.Error(DataError.Remote.UNKNOWN)
            }
        }

        HttpStatusCode.RequestTimeout.value -> Result.Error(DataError.Remote.REQUEST_TIMEOUT)
        HttpStatusCode.TooManyRequests.value -> Result.Error(DataError.Remote.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Remote.SERVER)
        else -> Result.Error(DataError.Remote.UNKNOWN)
    }
