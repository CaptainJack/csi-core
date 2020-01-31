package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.InternalConnectionImpl
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class ServerConnectionImpl<I : Any>(
	id: Long,
	override val identity: I,
	channel: InternalChannel,
	processor: ConnectionProcessor,
	assistant: DelayableAssistant,
	private val releaser: ServerConnectionReleaser<I>
) : InternalConnectionImpl(
	id,
	channel,
	processor,
	assistant,
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