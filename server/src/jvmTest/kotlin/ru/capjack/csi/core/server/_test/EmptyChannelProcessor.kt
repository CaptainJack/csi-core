package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.tool.io.FramedInputByteBuffer

object EmptyChannelProcessor : ChannelProcessor {
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		buffer.skipRead()
		return ChannelProcessorInputResult.CONTINUE
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}