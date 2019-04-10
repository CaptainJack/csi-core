package ru.capjack.tool.csi.server

import ru.capjack.tool.io.InputByteBuffer

interface Connection {
	val id: Any
	
	fun send(data: Byte)
	
	fun send(data: ByteArray)
	
	fun send(data: InputByteBuffer)
	
	fun close()
}