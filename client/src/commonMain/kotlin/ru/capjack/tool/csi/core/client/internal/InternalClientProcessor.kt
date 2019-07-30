package ru.capjack.tool.csi.core.client.internal

import ru.capjack.tool.csi.core.client.ClientDisconnectReason

internal interface InternalClientProcessor : InputProcessor {
	fun processDisconnect(reason: ClientDisconnectReason)
	
	fun processLoss()
}