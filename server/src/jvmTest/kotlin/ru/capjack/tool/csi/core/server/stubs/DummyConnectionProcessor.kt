package ru.capjack.tool.csi.core.server.stubs

import ru.capjack.tool.csi.core.server.internal.ConnectionDelegate
import ru.capjack.tool.csi.core.server.internal.ConnectionProcessor
import ru.capjack.tool.io.FramedInputByteBuffer

internal class DummyConnectionProcessor : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return false
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
	}
}
