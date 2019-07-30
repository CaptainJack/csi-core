package ru.capjack.tool.csi.core.client.stubs

import ru.capjack.tool.csi.core.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer

internal object DummyConnectionHandler : ConnectionHandler {
	override fun handleInput(data: InputByteBuffer) {
		data.readSkip()
	}
	
	override fun handleClose() {}
}
