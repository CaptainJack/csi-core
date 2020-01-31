package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer

interface ConnectionHandler {
	fun handleConnectionMessage(message: InputByteBuffer)
	
	fun handleConnectionClose()
}