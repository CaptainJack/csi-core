package ru.capjack.csi.core.internal

import ru.capjack.tool.io.InputByteBuffer


interface InternalChannelProcessor {
	fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult
	
	fun processChannelClose(channel: InternalChannel, interrupted: Boolean)
}