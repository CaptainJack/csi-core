package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.io.InputByteBuffer

internal object FakeConnectionDelegate : ConnectionDelegate {
	override val connectionId: String
		get() = throw UnsupportedOperationException()
	
	override fun setProcessor(processor: ConnectionProcessor) {
	}
	
	override fun close(reason: ConnectionCloseReason) {
	}
	
	override fun send(data: ByteArray) {
	}
	
	override fun send(data: InputByteBuffer) {
		data.skipRead()
	}
	
	override fun send(data: Byte) {
	}
	
	override fun terminate() {
	}
}
