package ru.capjack.csi.core.common

import ru.capjack.tool.io.InputByteBuffer


interface InternalChannelProcessor {
	fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult
	
	fun processChannelClose(channel: InternalChannel, interrupted: Boolean)
}