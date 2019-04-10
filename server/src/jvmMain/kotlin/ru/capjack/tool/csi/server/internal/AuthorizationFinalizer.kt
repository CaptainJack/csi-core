package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.server.Client
import ru.capjack.tool.csi.server.ClientAcceptor
import ru.capjack.tool.csi.server.ClientDisconnectHandler
import java.util.function.BiFunction

internal class AuthorizationFinalizer(
	private var client: InternalClient,
	private var clientAcceptor: ClientAcceptor
) : BiFunction<Long, InternalClient?, InternalClient>, ClientDisconnectHandler {
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