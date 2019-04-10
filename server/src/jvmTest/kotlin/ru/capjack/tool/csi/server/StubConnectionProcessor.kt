package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.server.internal.ConnectionDelegate
import ru.capjack.tool.csi.server.internal.ConnectionProcessor
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.readToArray

internal class StubConnectionProcessor(
	private val onProcessInput: (ConnectionDelegate, FramedInputByteBuffer) -> Unit = { _, _ -> },
	private val onProcessClose: () -> Unit = {}
) : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		onProcessInput(delegate, buffer)
		buffer.readToArray()
		return true
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
		onProcessClose()
	}
	
}