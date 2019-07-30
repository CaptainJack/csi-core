package ru.capjack.tool.csi.core.server

import ru.capjack.tool.io.InputByteBuffer

interface ClientMessageReceiver {
	fun receiveMessage(message: InputByteBuffer)
}