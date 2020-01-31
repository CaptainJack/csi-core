package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.FramedInputByteBuffer

internal class VersionValidatorChannelProcessor(
	private val serverVersion: Int,
	private val authorization: ChannelProcessor
) : ChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		if (buffer.isReadable(4)) {
			val clientVersion = buffer.readInt()
			
			if (clientVersion != serverVersion) {
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