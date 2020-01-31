package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.putLong
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class AcceptationConnectionProcessor<I : Any>(
	private val assistant: DelayableAssistant,
	private val connectionAcceptor: ConnectionAcceptor<I>,
	private val activityTimeoutSeconds: Int,
	private val identity: I,
	private val connectionId: Long
) : ConnectionProcessor {
	
	override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		channel.send(ByteArray(1 + 8).apply {
			set(0, ProtocolMarker.AUTHORIZATION)
			putLong(1, connectionId)
		})
		
		val handler = connectionAcceptor.acceptConnection(identity, connection)
		
		return ServerMessagingConnectionProcessor(handler, messages, assistant, activityTimeoutSeconds)
	}
	
	override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {}
	
	override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		throw ProtocolBrokenException()
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		connection.close()
		return NothingConnectionProcessor
	}
}
