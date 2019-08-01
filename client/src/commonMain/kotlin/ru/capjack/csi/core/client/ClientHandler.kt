package ru.capjack.csi.core.client

import ru.capjack.tool.io.InputByteBuffer

interface ClientHandler {
	fun handleMessage(message: InputByteBuffer)
	
	fun handleDisconnect(reason: ClientDisconnectReason)
	
	fun handleConnectionLost(): ConnectionRecoveryHandler
	
	fun handleServerShutdownTimeout(millis: Int)
}