package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer

internal object DummyConnectionHandler : ConnectionHandler {
	override fun handleInput(data: InputByteBuffer) {
		data.readSkip()
	}
	
	override fun handleClose() {}
}
