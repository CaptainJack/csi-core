package ru.capjack.csi.core.server

import ru.capjack.tool.io.InputByteBuffer

interface ClientHandler {
	fun handleMessage(message: InputByteBuffer)
	
	fun handleDisconnect()
}