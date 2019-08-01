package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.tool.io.InputByteBuffer

internal interface ConnectionDelegate {
	val connectionId: Any
	
	fun setProcessor(processor: ConnectionProcessor)
	
	fun send(data: Byte)
	
	fun send(data: ByteArray)
	
	fun send(data: InputByteBuffer)
	
	fun close(reason: ConnectionCloseReason)
	
	fun close()
	
	fun deferInput()
}