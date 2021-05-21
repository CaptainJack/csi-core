package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.ChannelProcessorInputResult
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.InternalConnection
import ru.capjack.csi.core.internal.ProtocolMarker
import ru.capjack.tool.io.InputByteBuffer

internal class RecoveryChannelProcessor(
	private val connection: InternalConnection
) : InternalChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		if (buffer.readByte() == ProtocolMarker.RECOVERY) {
			if (buffer.isReadable(4)) {
				
				val messageId = buffer.readInt()
				connection.recovery(channel, messageId)
				
				return ChannelProcessorInputResult.CONTINUE
			}
		}
		else {
			channel.close()
		}
		
		return ChannelProcessorInputResult.BREAK
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
		connection.close()
	}
}