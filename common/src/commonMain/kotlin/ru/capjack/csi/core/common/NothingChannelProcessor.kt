package ru.capjack.csi.core.common

import ru.capjack.tool.io.InputByteBuffer

object NothingChannelProcessor : ChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
		throw UnsupportedOperationException()
	}
}