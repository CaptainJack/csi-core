package ru.capjack.tool.csi.server

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray

class StubClientMessageReceiver(val client: Client) : ClientMessageReceiver {
	
	private val _input = ByteBuffer()
	val input: ByteArray
		get() = _input.readToArray()
	
	override fun receiveMessage(message: InputByteBuffer) {
		message.readBuffer(_input)
	}
}