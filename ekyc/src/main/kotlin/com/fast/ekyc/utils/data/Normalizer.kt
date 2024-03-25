package com.fast.ekyc.utils.data

internal fun <T> converter(source: String?, block: () -> T?): T? = try {
    if (source.isNullOrEmpty()) null
    else block()
} catch (e: Exception) {
    e.printStackTrace()
    null
}

internal interface Normalizable {
    fun normalize()
}

internal fun String?.nonEmptyOrNull(): String? = if (!this.isNullOrEmpty()) this else null

internal inline fun <reified T> norm(
    value: T?,
    default: T
) = value ?: default

internal inline fun <reified T : Any> norm(
    value: T?,
    crossinline factory: () -> T
): T {
    val default: T by lazy { factory() }
    return value ?: default
}

internal inline fun <reified T : Any> norm(value: List<T>?) = value ?: listOf()