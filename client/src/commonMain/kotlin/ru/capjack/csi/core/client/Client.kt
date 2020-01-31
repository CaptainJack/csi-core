package ru.capjack.csi.core.client

import ru.capjack.csi.core.client.internal.AuthorizationChannelAcceptor
import ru.capjack.csi.core.common.formatLoggerMessageBytes
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.utils.concurrency.DelayableAssistant

class Client(
	private val assistant: DelayableAssistant,
	private val channelGate: ChannelGate,
	private val version: Int = 0,
	private val activityTimeoutSeconds: Int = 30
) {
	fun connect(authorizationKey: ByteArray, connectionAcceptor: ConnectionAcceptor) {
		ownLogger.trace { formatLoggerMessageBytes("Connect with authorization key ", authorizationKey) }
		channelGate.openChannel(AuthorizationChannelAcceptor(assistant, channelGate, version, authorizationKey, connectionAcceptor, activityTimeoutSeconds))
	}
}

