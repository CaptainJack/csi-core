package ru.capjack.csi.core.server

interface ClientAcceptor {
	fun acceptClient(client: Client): ClientHandler
}

