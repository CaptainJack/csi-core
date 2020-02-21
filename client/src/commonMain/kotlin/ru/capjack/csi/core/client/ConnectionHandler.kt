package ru.capjack.csi.core.client

import ru.capjack.csi.core.BaseConnectionHandler

interface ConnectionHandler : BaseConnectionHandler {
	fun handleConnectionLost(): ConnectionRecoveryHandler
	
	fun handleConnectionCloseTimeout(seconds: Int)
}