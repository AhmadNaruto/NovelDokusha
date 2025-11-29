package my.noveldokusha.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.Response
import my.noveldokusha.core.flatMapError
import my.noveldokusha.core.flatten
import my.noveldokusha.core.tryAsResponse
import java.net.SocketTimeoutException

suspend fun <T> tryFlatConnect(
    extraErrorInfo: String = "",
    call: suspend () -> Response<T>
): Response<T> = tryAsResponse { call() }.flatten().specifyNetworkErrors(extraErrorInfo)

suspend fun <T> tryConnect(
    extraErrorInfo: String = "",
    call: suspend () -> T
): Response<T> = tryAsResponse { call() }.specifyNetworkErrors(extraErrorInfo)

// FIXED: Bungkus flatMapError di withContext untuk menjadikannya suspend-safe
private suspend fun <T> Response<T>.specifyNetworkErrors(extraErrorInfo: String = ""): Response<T> =
    withContext(Dispatchers.Default) {
        flatMapError { err ->
            val msg = err.exception.message ?: "No detail message"
            when (err.exception) {
                is SocketTimeoutException -> {
                    val error = listOf(
                        "Timeout error.",
                        "",
                        "Info:",
                        extraErrorInfo.ifBlank { "No info" },
                        "",
                        "Message:",
                        msg
                    ).joinToString("\n")
                    Response.Error(error, err.exception)
                }
                else -> {
                    val error = listOf(
                        "Unknown error.",
                        "",
                        "Info:",
                        extraErrorInfo.ifBlank { "No Info" },
                        "",
                        "Message:",
                        msg,
                        "",
                        "Stacktrace:",
                        err.exception.stackTraceToString()
                    ).joinToString("\n")
                    Response.Error(error, err.exception)
                }
            }
        }
    }
