package ru.capjack.csi.core.client

import ru.capjack.csi.core.client.internal.AuthorizationConnectionAcceptor
import ru.capjack.csi.core.formatLoggerMessageBytes
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.utils.concurrency.ScheduledExecutor

class ClientConnector(
	private val executor: ScheduledExecutor,
	private val connectionProducer: ConnectionProducer
) {
	fun connectClient(authorizationKey: ByteArray, acceptor: ClientAcceptor) {
		ownLogger.trace { formatLoggerMessageBytes("Connect with authorization key ", authorizationKey) }
		
		connectionProducer.produceConnection(
			AuthorizationConnectionAcceptor(executor, connectionProducer, authorizationKey, acceptor)
		)
	}
}

