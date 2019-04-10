package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.ClientDisconnectReason
import ru.capjack.tool.io.FramedInputByteBuffer

internal class FakeInternalClientProcessor : InternalClientProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processDisconnect(reason: ClientDisconnectReason) {
		throw UnsupportedOperationException()
	}
	
	override fun processLoss() {
		throw UnsupportedOperationException()
	}
}