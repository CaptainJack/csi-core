package ru.capjack.tool.csi.client

import ru.capjack.tool.csi.client.internal.AuthorizationConnectionAcceptor
import ru.capjack.tool.utils.concurrency.ScheduledExecutor

class ClientConnector(
	private val executor: ScheduledExecutor,
	private val connectionProducer: ConnectionProducer
) {
	fun connectClient(authorizationKey: ByteArray, acceptor: ClientAcceptor) {
		connectionProducer.produceConnection(
			AuthorizationConnectionAcceptor(executor, connectionProducer, authorizationKey, acceptor)
		)
	}
}

