package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientAcceptor
import ru.capjack.csi.core.server.ClientDisconnectHandler
import java.util.function.BiFunction

internal class AuthorizationFinalizer(
	private var client: InternalClient,
	private var clientAcceptor: ClientAcceptor
) : BiFunction<Long, InternalClient?, InternalClient>,
	ClientDisconnectHandler {
	override fun apply(clientId: Long, previous: InternalClient?): InternalClient {
		if (previous == null) {
			complete()
		}
		else {
			previous.addDisconnectHandler(this)
			previous.disconnectOfConcurrent()
		}
		
		return client
	}
	
	override fun handleClientDisconnect(client: Client) {
		complete()
	}
	
	private fun complete() {
		client.accept(clientAcceptor)
	}
}