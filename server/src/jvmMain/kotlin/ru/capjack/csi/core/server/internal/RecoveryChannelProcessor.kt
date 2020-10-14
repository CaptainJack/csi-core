package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.common.TransitionChannelProcessor
import ru.capjack.tool.io.InputByteBuffer

internal class RecoveryChannelProcessor(
	private val acceptor: ConnectionRecoveryAcceptor
) : ChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		/*return if (buffer.isReadable(8 + 4)) {
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
		}*/
		
		//TODO Legacy
		return if (buffer.isReadable(1 + 4 + 8)) {
			buffer.skipRead(1)
			val lastSentMessageId = buffer.readInt()
			val connectionId = buffer.readLong()
			
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
		//
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}

