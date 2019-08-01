package ru.capjack.csi.core.server.stubs

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientAcceptor
import ru.capjack.csi.core.server.ClientHandler
import ru.capjack.tool.io.InputByteBuffer

object DummyClientAcceptor : ClientAcceptor {
	override fun acceptClient(client: Client): ClientHandler {
		return object : ClientHandler {
			override fun handleMessage(message: InputByteBuffer) {}
			
			override fun handleDisconnect() {}
		}
	}
}