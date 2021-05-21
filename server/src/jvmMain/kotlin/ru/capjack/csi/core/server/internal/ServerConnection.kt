package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.internal.InternalConnection

internal interface ServerConnection<I : Any> : InternalConnection {
	val identity: I
}

