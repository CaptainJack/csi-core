package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer

internal interface ConnectionDelegate {
	val connectionId: Any
	
	fun setProcessor(processor: ConnectionProcessor)
	
	fun send(data: Byte)
	
	fun send(data: ByteArray)
	
	fun send(data: InputByteBuffer)
	
	fun close(reason: ConnectionCloseReason)
	
	fun close()
}