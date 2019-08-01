package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientAcceptor

internal interface InternalClient : Client {
	fun checkSessionKey(value: Long): Boolean
	
	fun accept(acceptor: ClientAcceptor)
	
	fun recovery(delegate: ConnectionDelegate, lastSentMessageId: Int)
	
	fun disconnectOfConcurrent()
}
