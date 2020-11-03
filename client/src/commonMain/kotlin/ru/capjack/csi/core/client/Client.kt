package ru.capjack.csi.core.client

import ru.capjack.csi.core.client.internal.AuthorizationChannelAcceptor
import ru.capjack.csi.core.common.formatLoggerMessageBytes
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool

class Client(
	private val assistant: TemporalAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val gate: ChannelGate,
	private val version: Int = 0,
	private val activityTimeoutSeconds: Int = 300
) {
	fun connect(authorizationKey: ByteArray, connectionAcceptor: ConnectionAcceptor) {
		ownLogger.trace { formatLoggerMessageBytes("Connect with authorization key ", authorizationKey) }
		
		gate.openChannel(
			AuthorizationChannelAcceptor(
				assistant,
				byteBuffers,
				gate,
				version,
				authorizationKey,
				connectionAcceptor,
				activityTimeoutSeconds
			)
		)
	}
}

