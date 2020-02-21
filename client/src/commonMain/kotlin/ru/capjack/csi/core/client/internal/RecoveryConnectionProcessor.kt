package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class RecoveryConnectionProcessor(
	private var messagingProcessor: ConnectionProcessor,
	private var recoveryHandler: ConnectionRecoveryHandler,
	assistant: DelayableAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	gate: ChannelGate,
	connection: InternalConnection,
	activityTimeoutSeconds: Int,
	lastIncomingMessageId: Int
) : ConnectionProcessor {
	
	init {
		assistant.schedule(10) {
			gate.openChannel(RecoveryChannelAcceptor(assistant, byteBuffers, connection, activityTimeoutSeconds, lastIncomingMessageId))
		}
	}
	
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel): ConnectionProcessor {
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
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	private fun free() {
		messagingProcessor = NothingConnectionProcessor
	}
}
