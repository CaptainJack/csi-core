package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.FramedInputByteBuffer

internal class RecoveryChannelProcessor(
	private val connection: InternalConnection
) : ChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
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