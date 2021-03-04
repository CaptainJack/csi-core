package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.common.InternalConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class AuthorizationConnectionProcessor(
	private val assistant: TemporalAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val activityTimeoutSeconds: Int,
	private val acceptor: ConnectionAcceptor,
	private val gate: ChannelGate
) : InternalConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		val handler = acceptor.acceptConnection(connection)
		return ClientMessagingConnectionProcessor(handler, connection.messages, connection.logger, assistant, byteBuffers, activityTimeoutSeconds, gate, channel)
	}
	
	override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw ProtocolBrokenException("Not expected incoming data")
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
		connection.close()
		return this
	}
	
	override fun processConnectionClose() {}
}