package ru.capjack.tool.csi.client

import ru.capjack.tool.csi.client.internal.AuthorizationConnectionAcceptor
import ru.capjack.tool.csi.common.formatLoggerMessageBytes
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
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

