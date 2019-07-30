package ru.capjack.tool.csi.core.server.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal object NothingConnectionProcessor : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
		throw UnsupportedOperationException()
	}
}
