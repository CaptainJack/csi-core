package ru.capjack.csi.core.common

import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.InputByteBuffer

object TransitionChannelProcessor : ChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		throw ProtocolBrokenException("Not expected incoming data")
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
	}
}