package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannelImpl
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class ClientChannelImpl(
	channel: Channel,
	processor: ChannelProcessor,
	assistant: DelayableAssistant,
	activityTimeoutSeconds: Int
) : InternalChannelImpl(channel, processor, assistant, activityTimeoutSeconds), ClientChannel {
	
	override fun processClose() {
	}
}