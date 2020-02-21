package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.InternalConnection

internal interface ServerConnection<I : Any> : InternalConnection {
	val identity: I
}

