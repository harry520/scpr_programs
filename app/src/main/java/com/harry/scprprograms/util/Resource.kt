package com.example.scprprograms.util

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun empty() = Resource(Status.EMPTY, null, null)
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)
        fun <T> error(msg: String, data: T?) = Resource(Status.ERROR, data, msg)
    }
}