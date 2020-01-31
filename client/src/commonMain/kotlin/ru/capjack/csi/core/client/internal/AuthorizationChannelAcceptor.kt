package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.putByteArray
import ru.capjack.tool.io.putInt
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class AuthorizationChannelAcceptor(
	private val assistant: DelayableAssistant,
	private val channelGate: ChannelGate,
	private val clientVersion: Int,
	private val authorizationKey: ByteArray,
	private val acceptor: ConnectionAcceptor,
	private val activityTimeoutSeconds: Int
) : ChannelAcceptor {
	
	override fun acceptChannel(channel: Channel): ChannelHandler {
		val clientChannel = ClientChannelImpl(
			channel,
			AuthorizationChannelProcessor(assistant, channelGate, acceptor, activityTimeoutSeconds),
			assistant,
			activityTimeoutSeconds
		)
		
		clientChannel.send(ByteArray(1 + 4 + 4 + authorizationKey.size).also {
			it[0] = ProtocolMarker.AUTHORIZATION
			it.putInt(1, 4 + authorizationKey.size)
			it.putInt(1 + 4, clientVersion)
			it.putByteArray(1 + 4 + 4, authorizationKey)
		})
		
		return clientChannel
	}
	
	override fun acceptFail() {
		acceptor.acceptFail(ConnectFailReason.REFUSED)
	}
}