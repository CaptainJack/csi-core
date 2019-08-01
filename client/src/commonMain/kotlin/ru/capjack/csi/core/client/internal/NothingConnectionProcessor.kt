package ru.capjack.csi.core.client.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal class NothingConnectionProcessor : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processLoss(delegate: ConnectionDelegate) {
		throw UnsupportedOperationException()
	}
	
}
