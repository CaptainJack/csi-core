package ru.capjack.tool.csi.server

import java.util.concurrent.ConcurrentHashMap

class StubClientAcceptor : ClientAcceptor {
	
	val receivers = ConcurrentHashMap<Long, StubClientMessageReceiver>()
	
	override fun acceptClient(clientId: Long, client: Client): ClientMessageReceiver {
		val receiver = StubClientMessageReceiver(client)
		receivers[clientId] = receiver
		return receiver
	}
}

