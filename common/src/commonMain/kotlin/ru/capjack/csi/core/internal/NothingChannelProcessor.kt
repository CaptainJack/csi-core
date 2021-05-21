package ru.capjack.csi.core.internal

import ru.capjack.tool.io.InputByteBuffer

object NothingChannelProcessor : InternalChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
		throw UnsupportedOperationException()
	}
}