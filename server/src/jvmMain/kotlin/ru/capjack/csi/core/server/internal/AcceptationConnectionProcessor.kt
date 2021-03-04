package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.common.InternalConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.io.putLong
import ru.capjack.tool.utils.assistant.TemporalAssistant

internal class AcceptationConnectionProcessor<I : Any>(
	private val assistant: TemporalAssistant,
	private val connectionAcceptor: ConnectionAcceptor<I>,
	private val activityTimeoutSeconds: Int,
	private val identity: I
) : InternalConnectionProcessor {
	
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		
		channel.send(ByteArray(1 + 8 + 4).apply {
			set(0, ProtocolMarker.AUTHORIZATION)
			putLong(1, connection.id)
			putInt(1 + 8, activityTimeoutSeconds)
		})
		
		val handler = connectionAcceptor.acceptConnection(identity, connection)
		
		return ServerMessagingConnectionProcessor(handler, connection.messages, connection.logger, assistant, activityTimeoutSeconds)
	}
	
	override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw ProtocolBrokenException("Not expected incoming data")
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
		connection.close()
		return this
	}
}
