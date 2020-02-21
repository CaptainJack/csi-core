package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannelImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class ClientChannelImpl(
	channel: Channel,
	processor: ChannelProcessor,
	assistant: DelayableAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	activityTimeoutSeconds: Int
) : InternalChannelImpl(channel, processor, byteBuffers, assistant, activityTimeoutSeconds), ClientChannel {
	
	override fun processClose() {
	}
}