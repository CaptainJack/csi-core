package ru.capjack.tool.csi.client

import ru.capjack.tool.io.InputByteBuffer

interface Client {
	fun sendMessage(data: Byte)
	
	fun sendMessage(data: ByteArray)

	fun sendMessage(data: InputByteBuffer)
	
	fun disconnect()
}

