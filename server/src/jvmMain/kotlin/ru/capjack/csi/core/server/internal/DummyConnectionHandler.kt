package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer

internal object DummyConnectionHandler : ConnectionHandler {
	override fun handleInput(data: InputByteBuffer) {
		data.skipRead()
	}
	
	override fun handleClose() {}
}
