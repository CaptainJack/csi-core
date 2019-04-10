package ru.capjack.tool.csi.server

import ru.capjack.tool.io.InputByteBuffer

interface ClientMessageReceiver {
	fun receiveMessage(message: InputByteBuffer)
}