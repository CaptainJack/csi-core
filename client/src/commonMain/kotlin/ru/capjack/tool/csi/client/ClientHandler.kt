package ru.capjack.tool.csi.client

import ru.capjack.tool.io.InputByteBuffer

interface ClientHandler {
	fun handleMessage(message: InputByteBuffer)
	
	fun handleDisconnect(reason: ClientDisconnectReason)
	
	fun handleConnectionLost(): ConnectionRecoveryHandler
}