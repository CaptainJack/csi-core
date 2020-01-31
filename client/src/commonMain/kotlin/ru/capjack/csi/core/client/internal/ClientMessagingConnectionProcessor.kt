package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ClientConnectionHandler
import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.MessagingConnectionProcessor
import ru.capjack.csi.core.common.NothingChannel
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.lang.alsoElse
import ru.capjack.tool.lang.alsoIf
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class ClientMessagingConnectionProcessor(
	handler: ClientConnectionHandler,
	messages: Messages,
	private val assistant: DelayableAssistant,
	private val activityTimeoutSeconds: Int,
	private val channelGate: ChannelGate,
	private var channel: Channel
) : MessagingConnectionProcessor<ClientConnectionHandler>(handler, messages) {
	
	private var pinger = Cancelable.DUMMY
	
	init {
		startPinger()
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		channel = NothingChannel
		stopPinger()
		val recoveryHandler = handler.handleConnectionLost()
		return RecoveryConnectionProcessor(this, recoveryHandler, assistant, channelGate, connection, activityTimeoutSeconds, messages.incoming.id)
	}
	
	override fun doProcessConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		this.channel = channel
		startPinger()
		return this
	}
	
	override fun doProcessConnectionClose(): ClientConnectionHandler {
		channel = NothingChannel
		stopPinger()
		return NothingClientConnectionHandler()
	}
	
	override fun processChannelInputMarker(channel: Channel, buffer: FramedInputByteBuffer, marker: Byte): Boolean {
		return when (marker) {
			ProtocolMarker.MESSAGING_PING          -> true
			ProtocolMarker.SERVER_CLOSE_SHUTDOWN   -> {
				channel.close()
				false
			}
			ProtocolMarker.SERVER_SHUTDOWN_TIMEOUT -> {
				buffer.isReadable(4)
					.alsoIf {
						val shutdownTimeoutSeconds = buffer.readInt()
						handler.handleConnectionCloseTimeout(shutdownTimeoutSeconds)
					}
					.alsoElse {
						buffer.backRead(1)
					}
			}
			
			else                                   -> super.processChannelInputMarker(channel, buffer, marker)
		}
	}
	
	private fun ping() {
		channel.send(ProtocolMarker.MESSAGING_PING)
	}
	
	private fun startPinger() {
		stopPinger()
		pinger = assistant.repeat(activityTimeoutSeconds * 1000, ::ping)
	}
	
	private fun stopPinger() {
		pinger.cancel()
		pinger = Cancelable.DUMMY
	}
}
