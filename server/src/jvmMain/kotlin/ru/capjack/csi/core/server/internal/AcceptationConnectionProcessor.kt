package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putLong
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class AcceptationConnectionProcessor<I : Any>(
	private val assistant: DelayableAssistant,
	private val connectionAcceptor: ConnectionAcceptor<I>,
	private val activityTimeoutSeconds: Int,
	private val identity: I
) : ConnectionProcessor {
	
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor {
		channel.send(ByteArray(1 + 8).apply {
			set(0, ProtocolMarker.AUTHORIZATION)
			putLong(1, connection.id)
		})
		
		val handler = connectionAcceptor.acceptConnection(identity, connection)
		
		return ServerMessagingConnectionProcessor(handler, connection.messages, connection.logger, assistant, activityTimeoutSeconds)
	}
	
	override fun processConnectionRecovery(channel: Channel): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw ProtocolBrokenException()
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		connection.close()
		return this
	}
}
