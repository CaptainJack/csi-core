package ru.capjack.tool.csi.core.server.stubs

import ru.capjack.tool.csi.core.server.Client
import ru.capjack.tool.csi.core.server.ClientAcceptor
import ru.capjack.tool.csi.core.server.ClientMessageReceiver

class StubClientAcceptor : ClientAcceptor {
	override fun acceptClient(clientId: Long, client: Client): ClientMessageReceiver {
		return StubClientMessageReceiver(client)
	}
}

