package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

object EmptyChannel : Channel {
	override val id: Any = 0
	
	override fun send(data: Byte) {}
	
	override fun send(data: ByteArray) {}
	
	override fun send(data: InputByteBuffer) {
		data.skipRead()
	}
	
	override fun close() {}
}

