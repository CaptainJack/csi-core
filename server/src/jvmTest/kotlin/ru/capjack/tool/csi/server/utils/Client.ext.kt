package ru.capjack.tool.csi.server.utils

import ru.capjack.tool.csi.server.Client
import ru.capjack.tool.csi.server.ClientDisconnectHandler


inline fun Client.addDisconnectHandler(crossinline handler: (Client) -> Unit) {
	addDisconnectHandler(object : ClientDisconnectHandler {
		override fun handleClientDisconnect(client: Client) {
			handler(client)
		}
	})
}
