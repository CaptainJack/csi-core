package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.core.client.DummyConnectionRecoveryHandler
import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class RecoveryConnectionProcessor(
	private var messagingProcessor: ConnectionProcessor,
	private var recoveryHandler: ConnectionRecoveryHandler,
	assistant: DelayableAssistant,
	channelGate: ChannelGate,
	connection: InternalConnection,
	activityTimeoutSeconds: Int,
	lastIncomingMessageId: Int
) : ConnectionProcessor {
	
	init {
		assistant.schedule(10) {
			channelGate.openChannel(RecoveryChannelAcceptor(assistant, connection, activityTimeoutSeconds, lastIncomingMessageId))
		}
	}
	
	override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		var p = messagingProcessor
		val h = recoveryHandler
		
		free()
		
		p = p.processConnectionRecovery(channel, lastSentMessageId)
		h.handleConnectionRecovered()
		
		return p
	}
	
	override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {
		with(messagingProcessor) {
			free()
			processConnectionClose()
		}
	}
	
	
	private fun free() {
		messagingProcessor = NothingConnectionProcessor
		recoveryHandler = DummyConnectionRecoveryHandler()
	}
	
}
