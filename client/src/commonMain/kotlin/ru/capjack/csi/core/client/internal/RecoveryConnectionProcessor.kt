package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.core.internal.InternalConnectionProcessor
import ru.capjack.csi.core.internal.InternalConnection
import ru.capjack.csi.core.internal.NothingConnectionProcessor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class RecoveryConnectionProcessor(
	private var messagingProcessor: InternalConnectionProcessor,
	private var recoveryHandler: ConnectionRecoveryHandler,
	assistant: TemporalAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	gate: ChannelGate,
	connection: InternalConnection,
	activityTimeoutSeconds: Int,
	lastIncomingMessageId: Int
) : InternalConnectionProcessor {
	
	init {
		assistant.schedule(10) {
			gate.openChannel(RecoveryChannelAcceptor(assistant, byteBuffers, connection, activityTimeoutSeconds, lastIncomingMessageId))
		}
	}
	
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		var p = messagingProcessor
		val h = recoveryHandler
		
		free()
		
		p = p.processConnectionRecovery(channel)
		h.handleConnectionRecovered()
		
		return p
	}
	
	override fun processConnectionClose() {
		with(messagingProcessor) {
			free()
			processConnectionClose()
		}
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	private fun free() {
		messagingProcessor = NothingConnectionProcessor
	}
}
