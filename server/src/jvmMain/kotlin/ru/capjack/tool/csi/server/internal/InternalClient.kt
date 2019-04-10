package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.server.Client
import ru.capjack.tool.csi.server.ClientAcceptor

internal interface InternalClient : Client {
	fun checkSessionKey(value: Long): Boolean
	
	fun accept(acceptor: ClientAcceptor)
	
	fun recovery(delegate: ConnectionDelegate, lastSentMessageId: Int)
	
	fun disconnectOfConcurrent()
}
