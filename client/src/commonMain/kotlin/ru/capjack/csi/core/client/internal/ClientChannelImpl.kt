package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.InternalChannelProcessor
import ru.capjack.csi.core.common.InternalChannelImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class ClientChannelImpl(
	channel: Channel,
	processor: InternalChannelProcessor,
	assistant: TemporalAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	activityTimeoutSeconds: Int
) : InternalChannelImpl(channel, processor, byteBuffers, assistant, activityTimeoutSeconds), ClientChannel {
	
	override fun processClose() {
	}
}