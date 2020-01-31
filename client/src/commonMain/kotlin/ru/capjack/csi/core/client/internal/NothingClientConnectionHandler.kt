package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ClientConnectionHandler
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.tool.io.InputByteBuffer

internal class NothingClientConnectionHandler : ClientConnectionHandler {
	override fun handleConnectionMessage(message: InputByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun handleConnectionClose() {
		throw UnsupportedOperationException()
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		throw UnsupportedOperationException()
	}
	
	override fun handleConnectionCloseTimeout(seconds: Int) {
		throw UnsupportedOperationException()
	}
}