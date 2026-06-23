package dyds.crypto.cecoin.core.domain.state

import dyds.crypto.cecoin.core.domain.error.AppError
import dyds.crypto.cecoin.core.domain.error.UiText
import dyds.crypto.cecoin.core.domain.error.fakeErrorClassifier

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GenericStatesTest {

    @Test
    fun `Loadable map on Loading returns Loading`() {
        val result = Loadable.Loading.map {  }
        assertIs<Loadable.Loading>(result)
    }

    @Test
    fun `Loadable map on Cancelled returns Cancelled`() {
        val result = Loadable.Cancelled.map {  }
        assertIs<Loadable.Cancelled>(result)
    }

    @Test
    fun `Loadable map on Loaded transforms value`() {
        val loaded: Loadable<Int> = Loadable.Loaded(42)
        val result = loaded.map { it * 2 }
        val loadedResult = assertIs<Loadable.Loaded<Int>>(result)
        assertEquals(84, loadedResult.value)
    }

    @Test
    fun `Fallible map on Success transforms value`() {
        val success: Fallible<Int> = Fallible.Success(42)
        val result = success.map { it * 2 }
        val successResult = assertIs<Fallible.Success<Int>>(result)
        assertEquals(84, successResult.value)
    }

    @Test
    fun `Fallible map on Failed returns same error`() {
        val error = AppError.GenericError(RuntimeException("fail"), UiText.Dynamic("msg"))
        val failed: Fallible<Int> = Fallible.Failed(error)
        val result = failed.map { it * 2 }
        val failedResult = assertIs<Fallible.Failed>(result)
        assertTrue(failedResult.error is AppError.GenericError)
    }

    @Test
    fun `runCatchingCancellable returns success when block succeeds`() = runTest {
        val result = runCatchingCancellable { 42 }
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `runCatchingCancellable returns failure when block throws`() = runTest {
        val result = runCatchingCancellable { throw RuntimeException("fail") }
        assertTrue(result.isFailure)
        assertIs<RuntimeException>(result.exceptionOrNull())
    }

    @Test
    fun `runCatchingCancellable rethrows CancellationException`() = runTest {
        assertFailsWith<CancellationException> {
            runCatchingCancellable { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `toFallible on success returns Success`() = runTest {
        val result = Result.success(42)
        val fallible = result.toFallible(fakeErrorClassifier()) { "test" }
        val success = assertIs<Fallible.Success<Int>>(fallible)
        assertEquals(42, success.value)
    }

    @Test
    fun `toFallible with network error returns NetworkError`() = runTest {
        val classifier = fakeErrorClassifier(isNetworkError = true)
        val result = Result.failure<Int>(RuntimeException("no network"))
        val fallible = result.toFallible(classifier) { "net msg" }
        val failed = assertIs<Fallible.Failed>(fallible)
        assertIs<AppError.NetworkError>(failed.error)
    }

    @Test
    fun `toFallible with generic error returns GenericError`() = runTest {
        val classifier = fakeErrorClassifier(isNetworkError = false)
        val result = Result.failure<Int>(RuntimeException("bad"))
        val fallible = result.toFallible(classifier) { "gen msg" }
        val failed = assertIs<Fallible.Failed>(fallible)
        val generic = assertIs<AppError.GenericError>(failed.error)
        assertIs<RuntimeException>(generic.exception)
    }

    @Test
    fun `Fallible onFailure on Success does not invoke action`() {
        var invoked = false
        val success: Fallible<Int> = Fallible.Success(42)
        success.onFailure { invoked = true }
        assertTrue(!invoked)
    }

    @Test
    fun `Fallible onFailure on Failed invokes action with error`() {
        val error = AppError.GenericError(RuntimeException("fail"), UiText.Dynamic("msg"))
        val failed: Fallible<Int> = Fallible.Failed(error)
        var captured: AppError? = null
        failed.onFailure { captured = it }
        assertIs<AppError.GenericError>(captured)
    }
}
