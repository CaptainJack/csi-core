package ru.capjack.csi.core.internal

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

object NothingChannel : Channel {
	override val id: Any
		get() = "nothing"
	
	override fun send(data: Byte) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: ByteArray) {
		throw UnsupportedOperationException()
	}
	
	override fun send(data: InputByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun close() {
		throw UnsupportedOperationException()
	}
}