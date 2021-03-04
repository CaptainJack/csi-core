package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.common.InternalChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class AuthorizationChannelProcessor(
	private val assistant: TemporalAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val gate: ChannelGate,
	private var acceptor: ConnectionAcceptor
) : InternalChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		val marker = buffer.readByte()
		if (marker == ProtocolMarker.AUTHORIZATION) {
			if (buffer.isReadable(8 + 4)) {
				val connectionId = buffer.readLong()
				val activityTimeoutSeconds = buffer.readInt()
				val connection = ClientConnectionImpl(
					connectionId,
					channel,
					AuthorizationConnectionProcessor(assistant, byteBuffers, activityTimeoutSeconds, acceptor, gate),
					assistant,
					byteBuffers
				)
				acceptor = NothingConnectionAcceptor()
				channel.useProcessor(connection, activityTimeoutSeconds)
				connection.accept()
				return ChannelProcessorInputResult.CONTINUE
			}
			
			buffer.backRead(1)
		}
		else when (marker) {
			ProtocolMarker.SERVER_CLOSE_VERSION       -> fail(channel, ConnectFailReason.VERSION)
			ProtocolMarker.SERVER_CLOSE_AUTHORIZATION -> fail(channel, ConnectFailReason.AUTHORIZATION)
			ProtocolMarker.SERVER_CLOSE_SHUTDOWN      -> fail(channel, ConnectFailReason.REFUSED)
			ProtocolMarker.CLOSE_DEFINITELY           -> fail(channel, ConnectFailReason.REFUSED)
			ProtocolMarker.CLOSE_ERROR                -> fail(channel, ConnectFailReason.ERROR)
			ProtocolMarker.CLOSE_PROTOCOL_BROKEN      -> fail(channel, ConnectFailReason.ERROR)
			ProtocolMarker.SERVER_CLOSE_CONCURRENT    -> fail(channel, ConnectFailReason.REFUSED)
			ProtocolMarker.SERVER_SHUTDOWN_TIMEOUT    -> fail(channel, ConnectFailReason.REFUSED, ProtocolMarker.CLOSE_DEFINITELY)
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
