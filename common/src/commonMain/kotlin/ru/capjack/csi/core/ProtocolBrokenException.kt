package ru.capjack.csi.core

class ProtocolBrokenException(message: String, cause: Throwable? = null) : CsiException(message, cause)

