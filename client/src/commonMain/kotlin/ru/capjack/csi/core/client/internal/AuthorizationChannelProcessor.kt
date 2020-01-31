package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class AuthorizationChannelProcessor(
	private val assistant: DelayableAssistant,
	private val channelGate: ChannelGate,
	private var acceptor: ConnectionAcceptor,
	private val activityTimeoutSeconds: Int
) : ChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		val marker = buffer.readByte()
		if (marker == ProtocolMarker.AUTHORIZATION) {
			if (buffer.isReadable(8)) {
				val connectionId = buffer.readLong()
				val connection = ClientConnectionImpl(
					connectionId,
					channel,
					AuthorizationConnectionProcessor(assistant, activityTimeoutSeconds, acceptor, channelGate),
					assistant
				)
				acceptor = NothingConnectionAcceptor()
				connection.accept()
				return ChannelProcessorInputResult.CONTINUE
			}
			
			buffer.backRead(1)
		}
		else when (marker) {
			ProtocolMarker.SERVER_CLOSE_VERSION       -> fail(channel, ConnectFailReason.VERSION)
			ProtocolMarker.SERVER_CLOSE_AUTHORIZATION -> fail(channel, ConnectFailReason.AUTHORIZATION)
			ProtocolMarker.SERVER_CLOSE_SHUTDOWN      -> fail(channel, ConnectFailReason.REFUSED)
			ProtocolMarker.CLOSE_ERROR                -> fail(channel, ConnectFailReason.ERROR)
			ProtocolMarker.CLOSE_PROTOCOL_BROKEN      -> fail(channel, ConnectFailReason.ERROR)
			ProtocolMarker.SERVER_SHUTDOWN_TIMEOUT    -> fail(channel, ConnectFailReason.REFUSED, ProtocolMarker.CLOSE_DEFINITELY)
			ProtocolMarker.CLOSE_DEFINITELY           -> fail(channel, ConnectFailReason.REFUSED)
			else                                      -> fail(channel, ConnectFailReason.ERROR, ProtocolMarker.CLOSE_PROTOCOL_BROKEN)
		}
		return ChannelProcessorInputResult.BREAK
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
		if (interrupted) {
			fail(ConnectFailReason.REFUSED)
		}
	}
	
	private fun fail(reason: ConnectFailReason) {
		with(acceptor) {
			acceptor = NothingConnectionAcceptor()
			acceptFail(reason)
		}
	}
	
	private fun fail(channel: InternalChannel, reason: ConnectFailReason) {
		fail(reason)
		channel.close()
	}
	
	private fun fail(channel: InternalChannel, reason: ConnectFailReason, marker: Byte) {
		fail(reason)
		channel.closeWithMarker(marker)
	}
}
