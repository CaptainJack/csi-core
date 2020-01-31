package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer

internal object NothingConnectionHandler : ConnectionHandler {
	override fun handleConnectionMessage(message: InputByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun handleConnectionClose() {
		throw UnsupportedOperationException()
	}
}
