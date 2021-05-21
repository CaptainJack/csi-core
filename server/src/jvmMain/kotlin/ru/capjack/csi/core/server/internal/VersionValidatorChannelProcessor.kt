package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.ChannelProcessorInputResult
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.ProtocolMarker
import ru.capjack.tool.io.InputByteBuffer

internal class VersionValidatorChannelProcessor(
	private val serverVersion: Int,
	private val authorization: InternalChannelProcessor
) : InternalChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		if (buffer.isReadable(4)) {
			val clientVersion = buffer.readInt()
			
			if (clientVersion < serverVersion) {
				channel.closeWithMarker(ProtocolMarker.SERVER_CLOSE_VERSION)
				return ChannelProcessorInputResult.BREAK
			}
			channel.useProcessor(authorization)
			return ChannelProcessorInputResult.CONTINUE
		}
		
		return ChannelProcessorInputResult.BREAK
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}