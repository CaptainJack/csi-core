package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.ProtocolFlag
import ru.capjack.csi.core.client.ClientAcceptor
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionProducer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.utils.concurrency.ScheduledExecutor

internal class AuthorizationConnectionAcceptor(
	private val executor: ScheduledExecutor,
	private val connectionProducer: ConnectionProducer,
	private val authorizationKey: ByteArray,
	private val acceptor: ClientAcceptor
) : ConnectionAcceptor {
	override fun acceptSuccess(connection: Connection): ConnectionHandler {
		val delegate = ConnectionDelegateImpl(
			executor,
			connection,
			AuthorizationConnectionProcessor(executor, connectionProducer, acceptor)
		)
		
		executor.execute {
			delegate.send(ByteArray(1 + 4 + authorizationKey.size).also {
				it[0] = ProtocolFlag.AUTHORIZATION
				it.putInt(1, authorizationKey.size)
				authorizationKey.copyInto(it, 5)
			})
		}
		
		return delegate
	}
	
	override fun acceptFail() {
		acceptor.acceptFail(ConnectFailReason.CONNECTION_REFUSED)
	}
}