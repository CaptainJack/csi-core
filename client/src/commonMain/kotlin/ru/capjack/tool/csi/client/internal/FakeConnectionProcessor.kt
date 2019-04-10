package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal class FakeConnectionProcessor : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processLoss(delegate: ConnectionDelegate) {
		throw UnsupportedOperationException()
	}
	
}
