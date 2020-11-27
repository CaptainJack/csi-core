package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.MessagingConnectionProcessor
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.alsoElse
import ru.capjack.tool.lang.alsoIf
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class ClientMessagingConnectionProcessor(
	handler: ConnectionHandler,
	messages: Messages,
	logger: Logger,
	private val assistant: TemporalAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val activityTimeoutSeconds: Int,
	private val gate: ChannelGate,
	channel: Channel
) : MessagingConnectionProcessor<ConnectionHandler>(handler, messages, logger) {
	
	private var pinger = Cancelable.DUMMY
	
	init {
		startPinger(channel)
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		stopPinger()
		val recoveryHandler = handler.handleConnectionLost()
		return RecoveryConnectionProcessor(this, recoveryHandler, assistant, byteBuffers, gate, connection, activityTimeoutSeconds, lastIncomingMessageId)
	}
	
	override fun doProcessConnectionRecovery(channel: Channel): ConnectionProcessor {
		startPinger(channel)
		return this
	}
	
	override fun doProcessConnectionClose(): ConnectionHandler {
		stopPinger()
		return NothingConnectionHandler()
	}
	
	override fun processChannelInputMarker(channel: Channel, buffer: InputByteBuffer, marker: Byte): Boolean {
		return when (marker) {
			ProtocolMarker.MESSAGING_PING          -> true
			ProtocolMarker.SERVER_CLOSE_SHUTDOWN   -> {
				channel.close()
				false
			}
			ProtocolMarker.SERVER_CLOSE_CONCURRENT -> {
				logger.warn("Closing because of the received marker ${ProtocolMarker.toString(marker)}")
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
	
	private fun startPinger(channel: Channel) {
		stopPinger()
		pinger = assistant.repeat(activityTimeoutSeconds * 1000) {
			channel.send(ProtocolMarker.MESSAGING_PING)
		}
	}
	
	private fun stopPinger() {
		pinger.cancel()
		pinger = Cancelable.DUMMY
	}
}
