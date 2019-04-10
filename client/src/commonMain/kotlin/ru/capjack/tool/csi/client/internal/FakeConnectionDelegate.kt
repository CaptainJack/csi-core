package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.io.InputByteBuffer

internal class FakeConnectionDelegate : ConnectionDelegate {
	override fun setProcessor(processor: ConnectionProcessor) {
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
	
	override fun terminate() {
	}
}
