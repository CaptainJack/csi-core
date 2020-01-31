package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.server.internal.ServerChannel
import ru.capjack.tool.io.InputByteBuffer

object NothingServerChannel : ServerChannel {
	override val id: Any
		get() = "nothing"
	
	override fun send(data: Byte) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: ByteArray) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: InputByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun close() {
		throw UnsupportedOperationException()
	}
	
	override fun closeWithMarker(marker: Byte) {
		throw UnsupportedOperationException()
	}
	
	override fun useProcessor(processor: ChannelProcessor) {
		throw UnsupportedOperationException()
	}
}