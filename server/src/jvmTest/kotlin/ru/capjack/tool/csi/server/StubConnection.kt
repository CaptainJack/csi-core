package ru.capjack.tool.csi.server

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import java.util.concurrent.atomic.AtomicInteger

class StubConnection(
	private val onSend: () -> Unit = {}

) : Connection {
	lateinit var handler: ConnectionHandler
	
	private val _output = ByteBuffer()
	val output: ByteArray
		get() = _output.readToArray()
	
	private val _closeCounter = AtomicInteger()
	val closeCounter: Int
		get() = _closeCounter.get()
	
	override val id = "#" + counter.incrementAndGet()
	
	override fun send(data: Byte) {
		onSend()
		_output.writeByte(data)
	}
	
	override fun send(data: ByteArray) {
		onSend()
		_output.writeArray(data)
	}
	
	override fun send(data: InputByteBuffer) {
		onSend()
		_output.writeBuffer(data)
	}
	
	override fun close() {
		_closeCounter.getAndIncrement()
	}
	
	companion object {
		val counter = AtomicInteger()
	}
}