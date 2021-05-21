package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.tool.io.InputByteBuffer

object EmptyInternalChannel : InternalChannel {
	override val id: Any = 0
	
	override fun useProcessor(processor: InternalChannelProcessor) {
	}
	
	override fun useProcessor(processor: InternalChannelProcessor, activityTimeoutSeconds: Int) {
	}
	
	override fun closeWithMarker(marker: Byte) {
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
}