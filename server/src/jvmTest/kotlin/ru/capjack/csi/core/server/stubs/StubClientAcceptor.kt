package ru.capjack.csi.core.server.stubs

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientAcceptor
import ru.capjack.csi.core.server.ClientHandler

class StubClientAcceptor : ClientAcceptor {
	override fun acceptClient(client: Client): ClientHandler {
		return StubClientHandler(client)
	}
}

