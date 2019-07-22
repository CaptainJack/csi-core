package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal object NothingInternalClientProcessor : InternalClientProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processLoss() {
		throw UnsupportedOperationException()
	}
	
	override fun processRecovery() {
		throw UnsupportedOperationException()
	}
}