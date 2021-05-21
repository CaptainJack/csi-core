package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.ChannelProcessorInputResult
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.ProtocolMarker
import ru.capjack.csi.core.internal.TransitionChannelProcessor
import ru.capjack.tool.io.InputByteBuffer

internal class RecoveryChannelProcessor(
	private val acceptor: ConnectionRecoveryAcceptor
) : InternalChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		return if (buffer.isReadable(8 + 4)) {
			val connectionId = buffer.readLong()
			val lastSentMessageId = buffer.readInt()
			
			val connection = acceptor.acceptRecovery(connectionId)
			
			if (connection == null) {
				channel.closeWithMarker(ProtocolMarker.SERVER_CLOSE_RECOVERY_FAIL)
				ChannelProcessorInputResult.BREAK
			}
			else {
				channel.useProcessor(TransitionChannelProcessor)
				connection.recovery(channel, lastSentMessageId)
				ChannelProcessorInputResult.CONTINUE
			}
		}
		else {
			ChannelProcessorInputResult.BREAK
		}
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}

