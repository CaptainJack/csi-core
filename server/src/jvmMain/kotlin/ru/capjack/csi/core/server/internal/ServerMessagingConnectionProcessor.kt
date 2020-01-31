package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.MessagingConnectionProcessor
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class ServerMessagingConnectionProcessor(
	handler: ConnectionHandler,
	messages: Messages,
	private val assistant: DelayableAssistant,
	private val activityTimeoutSeconds: Int
) : MessagingConnectionProcessor<ConnectionHandler>(handler, messages) {
	
	override fun doProcessConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		channel.send(ByteArray(1 + 4).apply {
			set(0, ProtocolMarker.RECOVERY)
			putInt(1, messages.incoming.id)
		})
		return this
	}
	
	override fun doProcessConnectionClose(): ConnectionHandler {
		return NothingConnectionHandler
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		return RecoveryConnectionProcessor(connection, this, assistant, activityTimeoutSeconds)
	}
	
	override fun processChannelInputMarker(channel: Channel, buffer: FramedInputByteBuffer, marker: Byte): Boolean {
		if (marker == ProtocolMarker.MESSAGING_PING) {
			channel.send(ProtocolMarker.MESSAGING_PING)
			return true
		}
		return super.processChannelInputMarker(channel, buffer, marker)
	}
}