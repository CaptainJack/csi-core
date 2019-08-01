package ru.capjack.csi.core.client.stubs

import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.InputByteBuffer

open class DummyConnection(override val id: Any = "dummy") : Connection {
	
	override fun send(data: Byte) {}
	
	override fun send(data: ByteArray) {}
	
	override fun send(data: InputByteBuffer) = data.skipRead()
	
	override fun close() {}
}