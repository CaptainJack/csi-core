package ru.capjack.csi.core.common

import ru.capjack.tool.io.FramedInputByteBuffer

interface ChannelProcessor {
	fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult
	
	fun processChannelClose(channel: InternalChannel, interrupted: Boolean)
}