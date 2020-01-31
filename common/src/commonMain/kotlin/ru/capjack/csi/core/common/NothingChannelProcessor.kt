package ru.capjack.csi.core.common

import ru.capjack.tool.io.FramedInputByteBuffer

object NothingChannelProcessor : ChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
		throw UnsupportedOperationException()
	}
}