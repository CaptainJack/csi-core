package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.ClientDisconnectReason

internal interface InternalClientProcessor : InputProcessor {
	fun processDisconnect(reason: ClientDisconnectReason)
	
	fun processLoss()
}