package ru.capjack.tool.csi.core.server.stubs

import ru.capjack.tool.csi.core.server.Client
import ru.capjack.tool.csi.core.server.ClientAcceptor
import ru.capjack.tool.csi.core.server.ClientMessageReceiver
import ru.capjack.tool.io.InputByteBuffer

object DummyClientAcceptor : ClientAcceptor {
	override fun acceptClient(clientId: Long, client: Client): ClientMessageReceiver {
		return object : ClientMessageReceiver {
			override fun receiveMessage(message: InputByteBuffer) {}
		}
	}
}