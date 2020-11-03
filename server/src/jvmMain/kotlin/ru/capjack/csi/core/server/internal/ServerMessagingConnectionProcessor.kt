package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.MessagingConnectionProcessor
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.io.putLong
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.assistant.TemporalAssistant

internal class ServerMessagingConnectionProcessor(
	handler: ConnectionHandler,
	messages: Messages,
	logger: Logger,
	private val assistant: TemporalAssistant,
	private val activityTimeoutSeconds: Int,
	private val connectionId: Long //TODO Legacy
) : MessagingConnectionProcessor<ConnectionHandler>(handler, messages, logger) {
	
	override fun doProcessConnectionRecovery(channel: Channel): ConnectionProcessor {
		/*channel.send(ByteArray(1 + 4).apply {
			set(0, ProtocolMarker.RECOVERY)
			putInt(1, lastIncomingMessageId)
		})*/
		
		//TODO Legacy
		channel.send(ByteArray(1 + 1 + 4 + 8).apply {
			set(0, ProtocolMarker.RECOVERY)
			set(1, 4 + 8)
			putInt(1 + 1, lastIncomingMessageId)
			putLong(1 + 1 + 4, connectionId)
		})
		//
		
		return this
	}
	
	override fun doProcessConnectionClose(): ConnectionHandler {
		return NothingConnectionHandler
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		return RecoveryConnectionProcessor(this, connection, assistant, activityTimeoutSeconds)
	}
	
	override fun processChannelInputMarker(channel: Channel, buffer: InputByteBuffer, marker: Byte): Boolean {
		if (marker == ProtocolMarker.MESSAGING_PING) {
			channel.send(ProtocolMarker.MESSAGING_PING)
			return true
		}
		return super.processChannelInputMarker(channel, buffer, marker)
	}
}
