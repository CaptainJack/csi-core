package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.InternalConnectionImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class ClientConnectionImpl(
	id: Long,
	channel: InternalChannel,
	processor: ConnectionProcessor,
	assistant: DelayableAssistant,
	byteBufferPool: ObjectPool<ByteBuffer>
) : InternalConnectionImpl(id, channel, processor, assistant, byteBufferPool, id.toHexString()), ClientConnection {
	override fun syncProcessClose() {}
}