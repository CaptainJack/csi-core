package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class AuthorizationConnectionProcessor(
	private val assistant: DelayableAssistant,
	private val activityTimeoutSeconds: Int,
	private val acceptor: ConnectionAcceptor,
	private val channelGate: ChannelGate
) : ConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		val handler = acceptor.acceptConnection(connection)
		return ClientMessagingConnectionProcessor(handler, messages, assistant, activityTimeoutSeconds, channelGate, channel)
	}
	
	override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		throw ProtocolBrokenException()
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		return this
	}
	
	override fun processConnectionClose() {
		throw UnsupportedOperationException()
	}
	
}