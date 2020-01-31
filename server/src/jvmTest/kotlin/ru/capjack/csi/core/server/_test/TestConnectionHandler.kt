package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import kotlin.concurrent.thread

class TestConnectionHandler(private val connection: Connection) : ConnectionHandler {
	private var sleepOnClose: Long = 0
	
	override fun handleConnectionMessage(message: InputByteBuffer) {
		if (message.readable) {
			when (message.readByte()) {
				TestApiMarker.ECHO        -> {
					val data = message.readToArray()
					connection.sendMessage(data)
				}
				TestApiMarker.SLEEP       -> {
					val ms = message.readInt().toLong()
					Thread.sleep(ms)
				}
				TestApiMarker.CLOSE       -> {
					connection.close()
				}
				TestApiMarker.CLOSE_SLEEP -> {
					sleepOnClose = message.readInt().toLong()
					thread {
						connection.close()
					}
					Thread.sleep(10)
				}
				TestApiMarker.SLEEP_CLOSE -> {
					val ms = message.readInt().toLong()
					Thread.sleep(ms)
					connection.close()
				}
				TestApiMarker.ERROR       -> {
					throw RuntimeException()
				}
				TestApiMarker.CLOSE_ERROR -> {
					connection.close()
					throw RuntimeException()
				}
				TestApiMarker.SEND_BYTE   -> {
					val close = message.readByte() == 1.toByte()
					val parallel = message.readByte() == 1.toByte()
					
					if (parallel) {
						thread { connection.sendMessage(7) }
						if (close) {
							Thread.sleep(10)
							connection.close()
						}
					}
					else {
						if (close) connection.close()
						connection.sendMessage(7)
					}
				}
				TestApiMarker.SEND_ARRAY  -> {
					val close = message.readByte() == 1.toByte()
					val parallel = message.readByte() == 1.toByte()
					val array = ByteArray(1) { 7 }
					
					if (parallel) {
						thread { connection.sendMessage(array) }
						if (close) {
							Thread.sleep(10)
							connection.close()
						}
					}
					else {
						if (close) connection.close()
						connection.sendMessage(array)
					}
				}
				TestApiMarker.SEND_BUFFER -> {
					val close = message.readByte() == 1.toByte()
					val parallel = message.readByte() == 1.toByte()
					val buffer = ArrayByteBuffer() { writeByte(7) }
					
					if (parallel) {
						thread { connection.sendMessage(buffer) }
						if (close) {
							Thread.sleep(10)
							connection.close()
						}
					}
					else {
						if (close) connection.close()
						connection.sendMessage(buffer)
					}
				}
			}
		}
	}
	
	override fun handleConnectionClose() {
		if (sleepOnClose > 0) {
			Thread.sleep(sleepOnClose)
		}
	}
}