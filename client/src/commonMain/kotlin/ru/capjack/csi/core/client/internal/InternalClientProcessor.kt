package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.OutgoingMessage
import ru.capjack.csi.core.client.ClientDisconnectReason

internal interface InternalClientProcessor : InputProcessor {
	fun processDisconnect(reason: ClientDisconnectReason)
	
	fun processLoss()
	
	fun sendMessage(message: OutgoingMessage)
}