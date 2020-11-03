package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannelImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

class ServerChannelImpl(
	channel: Channel,
	processor: ChannelProcessor,
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
