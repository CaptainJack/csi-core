package ru.capjack.tool.csi.core.server.internal

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.io.InputByteBuffer

internal object NothingConnectionDelegate : ConnectionDelegate {
	override val connectionId: String
		get() = "nothing"
	
	override fun setProcessor(processor: ConnectionProcessor) {
		throw UnsupportedOperationException()
	}
	
	override fun close(reason: ConnectionCloseReason) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: ByteArray) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: InputByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: Byte) {
		throw UnsupportedOperationException()
	}
	
	override fun close() {
		throw UnsupportedOperationException()
	}
}
