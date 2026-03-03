package my.noveldokusha.network

import my.noveldokusha.core.Response
import my.noveldokusha.core.flatMapError
import my.noveldokusha.core.flatten
import my.noveldokusha.core.tryAsResponse
import java.net.SocketTimeoutException

/**
 * Executes a network call with error handling, flattening nested responses.
 * @param extraErrorInfo Additional context to include in error messages.
 * @param call The suspend function to execute.
 */
suspend fun <T> tryFlatConnect(
    extraErrorInfo: String = "",
    call: suspend () -> Response<T>
): Response<T> = tryAsResponse { call() }.flatten().handleNetworkErrors(extraErrorInfo)

/**
 * Executes a network call with error handling.
 * @param extraErrorInfo Additional context to include in error messages.
 * @param call The suspend function to execute.
 */
suspend fun <T> tryConnect(
    extraErrorInfo: String = "",
    call: suspend () -> T
): Response<T> = tryAsResponse { call() }.handleNetworkErrors(extraErrorInfo)

/**
 * Handles network-specific errors and provides detailed error messages.
 */
private suspend fun <T> Response<T>.handleNetworkErrors(extraErrorInfo: String = "") =
    flatMapError { error ->
        when (error.exception) {
            is SocketTimeoutException -> {
                val errorMessage = buildString {
                    appendLine("Timeout error.")
                    appendLine()
                    appendLine("Info:")
                    appendLine(extraErrorInfo.ifBlank { "No info" })
                    appendLine()
                    appendLine("Message:")
                    append(error.exception.message)
                }
                Response.Error(errorMessage, error.exception)
            }
            else -> {
                val errorMessage = buildString {
                    appendLine("Unknown error.")
                    appendLine()
                    appendLine("Info:")
                    appendLine(extraErrorInfo.ifBlank { "No Info" })
                    appendLine()
                    appendLine("Message:")
                    appendLine(error.exception.message)
                    appendLine()
                    appendLine("Stacktrace:")
                    append(error.exception.stackTraceToString())
                }
                Response.Error(errorMessage, error.exception)
            }
        }
    }
