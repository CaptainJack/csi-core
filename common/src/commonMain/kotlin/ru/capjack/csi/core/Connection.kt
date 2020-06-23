package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer

interface Connection {
	val id: Long
	val loggingName: String
	
	fun sendMessage(data: Byte)
	
	fun sendMessage(data: ByteArray)
	
	fun sendMessage(data: InputByteBuffer)
	
	fun close()
	
	fun close(handler: () -> Unit)
}