package ru.capjack.tool.csi.core.client.internal

import ru.capjack.tool.io.InputByteBuffer

internal interface ConnectionDelegate {
	val connectionId: Any
	
	fun setProcessor(processor: ConnectionProcessor)
	
	fun send(data: Byte)
	
	fun send(data: ByteArray)
	
	fun send(data: InputByteBuffer)
	
	fun close()
	
	fun terminate()
}