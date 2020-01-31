package ru.capjack.csi.core.client

import ru.capjack.csi.core.ConnectionHandler

interface ClientConnectionHandler : ConnectionHandler {
	fun handleConnectionLost(): ConnectionRecoveryHandler
	
	fun handleConnectionCloseTimeout(seconds: Int)
}