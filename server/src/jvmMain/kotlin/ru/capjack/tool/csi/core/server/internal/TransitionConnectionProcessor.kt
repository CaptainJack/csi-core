package ru.capjack.tool.csi.core.server.internal

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.io.FramedInputByteBuffer

internal object TransitionConnectionProcessor : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		delegate.close(ConnectionCloseReason.PROTOCOL_BROKEN)
		return false
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}
