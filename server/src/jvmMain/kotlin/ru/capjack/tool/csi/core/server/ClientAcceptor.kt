package ru.capjack.tool.csi.core.server

interface ClientAcceptor {
	fun acceptClient(clientId: Long, client: Client): ClientMessageReceiver
}

