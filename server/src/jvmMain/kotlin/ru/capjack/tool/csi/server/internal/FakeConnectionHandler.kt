package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.server.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer

internal object FakeConnectionHandler : ConnectionHandler {
	override fun handleInput(data: InputByteBuffer) {
		data.skipRead()
	}
	
	override fun handleClose() {}
}
