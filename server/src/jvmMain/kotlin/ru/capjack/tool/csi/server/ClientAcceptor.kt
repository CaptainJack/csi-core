package ru.capjack.tool.csi.server

interface ClientAcceptor {
	fun acceptClient(clientId: Long, client: Client): ClientMessageReceiver
}

