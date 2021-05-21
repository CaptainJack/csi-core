package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.ChannelProcessorInputResult
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.ProtocolMarker
import ru.capjack.tool.io.InputByteBuffer

internal class ReceptionChannelProcessor(
	private val authorization: InternalChannelProcessor,
	private val recovery: InternalChannelProcessor
) : InternalChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		return when (val marker = buffer.readByte()) {
			ProtocolMarker.AUTHORIZATION -> {
				channel.useProcessor(authorization)
				ChannelProcessorInputResult.CONTINUE
			}
			ProtocolMarker.RECOVERY      -> {
				channel.useProcessor(recovery)
				ChannelProcessorInputResult.CONTINUE
			}
			ProtocolMarker.CLOSE_DEFINITELY,
			ProtocolMarker.CLOSE_PROTOCOL_BROKEN,
			ProtocolMarker.CLOSE_ERROR   -> {
				channel.close()
				ChannelProcessorInputResult.BREAK
			}
			else                         -> throw ProtocolBrokenException("Unknown marker ${ProtocolMarker.toString(marker)}")
		}
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}

