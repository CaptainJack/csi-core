package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal interface ConnectionProcessor {
	fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean
	
	fun processClose(delegate: ConnectionDelegate, loss: Boolean)
}
