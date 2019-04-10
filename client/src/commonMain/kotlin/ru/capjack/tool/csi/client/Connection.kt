package ru.capjack.tool.csi.client

import ru.capjack.tool.io.InputByteBuffer

interface Connection {
	fun send(data: Byte)
	
	fun send(data: ByteArray)
	
	fun send(data: InputByteBuffer)
	
	fun close()
}