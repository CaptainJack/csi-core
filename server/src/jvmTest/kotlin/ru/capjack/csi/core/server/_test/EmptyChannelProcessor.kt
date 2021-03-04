package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.common.InternalChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.tool.io.InputByteBuffer

object EmptyChannelProcessor : InternalChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		buffer.skipRead()
		return ChannelProcessorInputResult.CONTINUE
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}