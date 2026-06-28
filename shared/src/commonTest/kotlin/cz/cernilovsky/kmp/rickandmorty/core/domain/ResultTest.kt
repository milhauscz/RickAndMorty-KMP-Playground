package cz.cernilovsky.kmp.rickandmorty.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultTest {

    private val anError = DataError.Remote.SERVER

    @Test
    fun `map transforms success data`() {
        val result: Result<Int, DataError> = Result.Success(2)

        val mapped = result.map { it * 10 }

        assertEquals(Result.Success(20), mapped)
    }

    @Test
    fun `map leaves error untouched`() {
        val result: Result<Int, DataError> = Result.Error(anError)

        val mapped = result.map { it * 10 }

        assertEquals(Result.Error(anError), mapped)
    }

    @Test
    fun `asEmptyDataResult discards success data`() {
        val result: Result<Int, DataError> = Result.Success(99)

        assertEquals(Result.Success(Unit), result.asEmptyDataResult())
    }

    @Test
    fun `onSuccess runs only on success`() {
        var captured: Int? = null

        val result: Result<Int, DataError> = Result.Success(5)
        result.onSuccess { captured = it }
        assertEquals(5, captured)

        captured = null
        val error: Result<Int, DataError> = Result.Error(anError)
        error.onSuccess { captured = it }
        assertEquals(null, captured)
    }

    @Test
    fun `onError runs only on error`() {
        var captured: DataError? = null

        val error: Result<Int, DataError> = Result.Error(anError)
        error.onError { captured = it }
        assertEquals(anError, captured)

        captured = null
        val success: Result<Int, DataError> = Result.Success(1)
        success.onError { captured = it }
        assertEquals(null, captured)
    }

    @Test
    fun `onSuccess returns the same result for chaining`() {
        val result: Result<Int, DataError> = Result.Success(1)

        assertTrue(result === result.onSuccess { })
    }
}
