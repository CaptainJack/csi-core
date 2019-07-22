package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.ClientAcceptor
import ru.capjack.tool.csi.client.ConnectFailReason
import ru.capjack.tool.csi.client.ConnectionAcceptor
import ru.capjack.tool.csi.common.ConnectionHandler
import ru.capjack.tool.csi.client.ConnectionProducer
import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.io.putInt
import ru.capjack.tool.utils.concurrency.ScheduledExecutor

internal class AuthorizationConnectionAcceptor(
	private val executor: ScheduledExecutor,
	private val connectionProducer: ConnectionProducer,
	private val authorizationKey: ByteArray,
	private val acceptor: ClientAcceptor
) : ConnectionAcceptor {
	override fun acceptSuccess(connection: Connection): ConnectionHandler {
		val delegate = ConnectionDelegateImpl(executor, connection, AuthorizationConnectionProcessor(executor, connectionProducer, acceptor))
		
		delegate.send(ByteArray(1 + 4 + authorizationKey.size).also {
			it[0] = ProtocolFlag.AUTHORIZATION
			it.putInt(1, authorizationKey.size)
			authorizationKey.copyInto(it, 5)
		})
		
		return delegate
	}
	
	override fun acceptFail() {
		acceptor.acceptFail(ConnectFailReason.CONNECTION_REFUSED)
	}
}