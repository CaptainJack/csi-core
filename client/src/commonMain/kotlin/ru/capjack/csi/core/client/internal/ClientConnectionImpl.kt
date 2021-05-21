package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.InternalConnectionImpl
import ru.capjack.csi.core.internal.InternalConnectionProcessor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

internal class ClientConnectionImpl(
	id: Long,
	channel: InternalChannel,
	processor: InternalConnectionProcessor,
	assistant: TemporalAssistant,
	byteBufferPool: ObjectPool<ByteBuffer>
) : InternalConnectionImpl(id, channel, processor, assistant, byteBufferPool, id.toHexString()), ClientConnection {
	override fun syncProcessClose() {}
}