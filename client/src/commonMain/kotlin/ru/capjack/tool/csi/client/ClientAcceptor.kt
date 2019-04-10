package ru.capjack.tool.csi.client

interface ClientAcceptor {
	fun acceptSuccess(client: Client): ClientHandler
	
	fun acceptFail(reason: ConnectFailReason)
}
