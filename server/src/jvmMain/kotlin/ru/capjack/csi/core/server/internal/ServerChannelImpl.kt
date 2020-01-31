package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannelImpl
import ru.capjack.tool.utils.concurrency.DelayableAssistant

class ServerChannelImpl(
	channel: Channel,
	processor: ChannelProcessor,
	assistant: DelayableAssistant,
	activityTimeoutSeconds: Int,
	private var releaser: ServerChannelReleaser
) : InternalChannelImpl(channel, processor, assistant, activityTimeoutSeconds), ServerChannel {
	
	override fun processClose() {
		releaser.releaseServerChannel(this)
		releaser = NothingServerChannelReleaser
	}
}
