package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer

interface BaseConnectionHandler {
	fun handleConnectionMessage(message: InputByteBuffer)
	
	fun handleConnectionClose()
}