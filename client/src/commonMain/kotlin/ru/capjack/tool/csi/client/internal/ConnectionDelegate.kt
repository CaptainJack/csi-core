package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.io.InputByteBuffer

internal interface ConnectionDelegate {
	fun setProcessor(processor: ConnectionProcessor)
	
	fun send(data: Byte)
	
	fun send(data: ByteArray)
	
	fun send(data: InputByteBuffer)
	
	fun close()
	
	fun terminate()
}