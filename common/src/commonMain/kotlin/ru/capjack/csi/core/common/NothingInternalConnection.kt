package ru.capjack.csi.core.common

import ru.capjack.tool.io.InputByteBuffer

object NothingInternalConnection : InternalConnection {
	override val id: Long
		get() = 0
	
	override fun accept() {
		throw UnsupportedOperationException()
	}
	
	override fun recovery(channel: InternalChannel, lastSentMessageId: Int) {
		throw UnsupportedOperationException()
	}
	
	override fun sendMessage(data: Byte) {
		throw UnsupportedOperationException()
	}
	
	override fun sendMessage(data: ByteArray) {
		throw UnsupportedOperationException()
	}
	
	override fun sendMessage(data: InputByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun close() {
		throw UnsupportedOperationException()
	}
	
	override fun close(handler: () -> Unit) {
		throw UnsupportedOperationException()
	}
	
	override fun closeWithMarker(marker: Byte) {
		throw UnsupportedOperationException()
	}
	
	override fun closeWithMarker(marker: Byte, handler: () -> Unit) {
		throw UnsupportedOperationException()
	}
}