package ru.capjack.csi.core.server

@FunctionalInterface
interface ClientDisconnectHandler {
	fun handleClientDisconnect(client: Client)
}