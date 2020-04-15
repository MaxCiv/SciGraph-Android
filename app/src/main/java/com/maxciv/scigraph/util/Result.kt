package com.maxciv.scigraph.util

/**
 * @author maxim.oleynik
 * @since 15.04.2020
 */
sealed class Result<out T: Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Fail(val message: String, val throwable: Throwable) : Result<Nothing>()
}
