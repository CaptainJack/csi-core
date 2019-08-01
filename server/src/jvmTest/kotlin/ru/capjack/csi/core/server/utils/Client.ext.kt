package ru.capjack.csi.core.server.utils

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientDisconnectHandler

inline fun Client.addDisconnectHandler(crossinline handler: (Client) -> Unit) {
	addDisconnectHandler(object : ClientDisconnectHandler {
		override fun handleClientDisconnect(client: Client) {
			handler(client)
		}
	})
}
