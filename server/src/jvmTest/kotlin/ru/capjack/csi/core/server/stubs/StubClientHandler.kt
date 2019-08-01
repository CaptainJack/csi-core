package ru.capjack.csi.core.server.stubs

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientHandler
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class StubClientHandler(private val client: Client) : ClientHandler {
	override fun handleMessage(message: InputByteBuffer) {
		when (message.readByte()) {
			0x00.toByte() -> client.disconnect()
			
			0x01.toByte() -> client.sendMessage(message.readByte())
			0x02.toByte() -> client.sendMessage(message.readToArray())
			0x03.toByte() -> client.sendMessage(ArrayByteBuffer(message))
			
			0x11.toByte() -> message.readByte().also { thread { client.sendMessage(it) } }
			0x12.toByte() -> message.readToArray().also { thread { client.sendMessage(it) } }
			0x13.toByte() -> message.readToArray().also { thread { client.sendMessage(ArrayByteBuffer(it)) } }
			
			0x04.toByte() -> sleep(message.readInt().toLong())
			
			0x05.toByte() -> throw RuntimeException("Server error")
			
			0x06.toByte() -> Unit
			
			0x07.toByte() -> {
				sleep(message.readInt().toLong())
				client.disconnect()
			}
			
			else          -> throw RuntimeException("Bad message")
		}
	}
	
	override fun handleDisconnect() {
	}
}