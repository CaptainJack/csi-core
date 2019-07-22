package ru.capjack.tool.csi.server.stubs

import ru.capjack.tool.csi.server.Client
import ru.capjack.tool.csi.server.ClientAcceptor
import ru.capjack.tool.csi.server.ClientMessageReceiver

class StubClientAcceptor : ClientAcceptor {
	override fun acceptClient(clientId: Long, client: Client): ClientMessageReceiver {
		return StubClientMessageReceiver(client)
	}
}

