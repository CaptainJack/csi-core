package ru.capjack.tool.csi.core.client

interface ClientAcceptor {
	fun acceptSuccess(client: Client): ClientHandler
	
	fun acceptFail(reason: ConnectFailReason)
}
