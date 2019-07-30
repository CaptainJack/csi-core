package ru.capjack.tool.csi.core.server

@FunctionalInterface
interface ClientDisconnectHandler {
	fun handleClientDisconnect(client: Client)
}