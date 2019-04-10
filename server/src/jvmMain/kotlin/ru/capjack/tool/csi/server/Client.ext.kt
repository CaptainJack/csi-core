package ru.capjack.tool.csi.server

inline fun Client.addDisconnectHandler(crossinline handler: (Client) -> Unit) {
	addDisconnectHandler(object : ClientDisconnectHandler {
		override fun handleClientDisconnect(client: Client) {
			handler(client)
		}
	})
}
