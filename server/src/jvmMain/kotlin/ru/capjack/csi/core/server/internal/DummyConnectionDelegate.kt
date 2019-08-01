package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.tool.io.InputByteBuffer

internal object DummyConnectionDelegate : ConnectionDelegate {
	override val connectionId: Any = "dummy"
	
	override fun setProcessor(processor: ConnectionProcessor) {
	}
	
	override fun close(reason: ConnectionCloseReason) {
	}
	
	override fun send(data: Byte) {
	}
	
	override fun send(data: ByteArray) {
	}
	
	override fun send(data: InputByteBuffer) {
		data.skipRead()
	}
	
	override fun close() {
	}
	
	override fun deferInput() {
	}
}
