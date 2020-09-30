package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler
import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.io.putLong
import ru.capjack.tool.utils.assistant.DelayableAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class RecoveryChannelAcceptor(
	private val assistant: DelayableAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val connection: InternalConnection,
	private val activityTimeoutSeconds: Int,
	private val lastIncomingMessageId: Int
) : ChannelAcceptor {
	
	override fun acceptChannel(channel: Channel): ChannelHandler {
		val clientChannel = ClientChannelImpl(
			channel,
			RecoveryChannelProcessor(connection),
			assistant,
			byteBuffers,
			activityTimeoutSeconds
		)
		
		clientChannel.send(ByteArray(1 + 8 + 4).also {
			it[0] = ProtocolMarker.RECOVERY
			it.putLong(1, connection.id)
			it.putInt(1 + 8, lastIncomingMessageId)
		})
		
		return clientChannel
	}
	
	override fun acceptFail() {
		connection.close()
	}
}