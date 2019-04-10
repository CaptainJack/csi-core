package ru.capjack.tool.csi.server

interface ClientDisconnectHandler {
	fun handleClientDisconnect(client: Client)
}