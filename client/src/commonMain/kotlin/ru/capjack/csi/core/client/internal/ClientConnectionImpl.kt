package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.InternalConnectionImpl
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class ClientConnectionImpl(
	id: Long,
	channel: InternalChannel,
	processor: ConnectionProcessor,
	assistant: DelayableAssistant
) : InternalConnectionImpl(id, channel, processor, assistant, id.toHexString()), ClientConnection {
	override fun syncProcessClose() {}
}