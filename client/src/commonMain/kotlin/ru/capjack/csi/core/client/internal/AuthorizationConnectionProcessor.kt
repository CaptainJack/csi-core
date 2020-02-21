package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class AuthorizationConnectionProcessor(
	private val assistant: DelayableAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val activityTimeoutSeconds: Int,
	private val acceptor: ConnectionAcceptor,
	private val gate: ChannelGate
) : ConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor {
		val handler = acceptor.acceptConnection(connection)
		return ClientMessagingConnectionProcessor(handler, connection.messages, connection.logger, assistant, byteBuffers, activityTimeoutSeconds, gate, channel)
	}
	
	override fun processConnectionRecovery(channel: Channel): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw ProtocolBrokenException()
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		connection.close()
		return this
	}
	
	override fun processConnectionClose() {}
}