package ru.capjack.tool.csi.server

@FunctionalInterface
interface ClientDisconnectHandler {
	fun handleClientDisconnect(client: Client)
}