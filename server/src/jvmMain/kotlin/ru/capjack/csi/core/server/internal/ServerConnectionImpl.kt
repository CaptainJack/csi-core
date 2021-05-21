package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.internal.InternalConnectionProcessor
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.InternalConnectionImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class ServerConnectionImpl<I : Any>(
	id: Long,
	override val identity: I,
	channel: InternalChannel,
	processor: InternalConnectionProcessor,
	assistant: TemporalAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	private val releaser: ServerConnectionReleaser<I>
) : InternalConnectionImpl(
	id,
	channel,
	processor,
	assistant,
	byteBuffers,
	"$identity-${id.toHexString()}"
), ServerConnection<I> {
	
	override fun tryRecovery(channel: InternalChannel, lastSentMessageId: Int): Boolean {
		return !waitIf(1000, 10) {
			!super.tryRecovery(channel, lastSentMessageId)
		}
	}
	
	override fun syncProcessClose() {
		releaser.releaseServerConnection(this)
	}
}