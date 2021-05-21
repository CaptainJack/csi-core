package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.internal.InternalConnectionProcessor
import ru.capjack.csi.core.internal.InternalConnection
import ru.capjack.csi.core.internal.Messages
import ru.capjack.csi.core.internal.MessagingConnectionProcessor
import ru.capjack.csi.core.internal.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.assistant.TemporalAssistant

internal class ServerMessagingConnectionProcessor(
	handler: ConnectionHandler,
	messages: Messages,
	logger: Logger,
	private val assistant: TemporalAssistant,
	private val activityTimeoutSeconds: Int
) : MessagingConnectionProcessor<ConnectionHandler>(handler, messages, logger) {
	
	override fun doProcessConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		channel.send(ByteArray(1 + 4).apply {
			set(0, ProtocolMarker.RECOVERY)
			putInt(1, lastIncomingMessageId)
		})
		return this
	}
	
	override fun doProcessConnectionClose(): ConnectionHandler {
		return NothingConnectionHandler
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
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
