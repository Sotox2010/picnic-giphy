package com.jesussoto.android.giphy.api

data class Resource<T> private constructor(
        val status: Status,
        val data: T?,
        val throwable: Throwable?) {

    enum class Status {
        LOADING, SUCCESS, ERROR
    }

    companion object {

        @JvmStatic
        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        @JvmStatic
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        @JvmStatic
        fun <T> error(throwable: Throwable, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, throwable)
        }
    }
}
