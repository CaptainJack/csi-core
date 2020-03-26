package ru.capjack.csi.core

open class CsiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)