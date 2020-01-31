package ru.capjack.csi.core.common

import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.FramedInputByteBuffer

object TransitionChannelProcessor : ChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		throw ProtocolBrokenException()
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
	}
}