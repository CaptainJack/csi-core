package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.InternalChannelImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

class ServerChannelImpl(
	channel: Channel,
	processor: InternalChannelProcessor,
	byteBuffers: ObjectPool<ByteBuffer>,
	assistant: TemporalAssistant,
	activityTimeoutSeconds: Int,
	private var releaser: ServerChannelReleaser
) : InternalChannelImpl(channel, processor, byteBuffers, assistant, activityTimeoutSeconds), ServerChannel {
	
	override fun processClose() {
		releaser.releaseServerChannel(this)
		releaser = NothingServerChannelReleaser
	}
}
