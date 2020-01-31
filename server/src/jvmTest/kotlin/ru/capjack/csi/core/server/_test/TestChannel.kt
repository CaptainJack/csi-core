package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.lang.waitIf
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

val channelOutputAssistant = assistant(8, "channel-output")

class TestChannel(
	override val id: Any
) : Channel {
	@Volatile
	private var closed: Boolean = false
	
	@Volatile
	private var handler: ChannelHandler? = null
	
	private val input: BlockingQueue<Change> = LinkedBlockingQueue()
	private val output: BlockingQueue<Change> = LinkedBlockingQueue()
	
	val completed: Boolean
		get() = closed && input.isEmpty() && output.isEmpty()
	
	val state: String
		get() = """
			channel $id
			  closed: $closed
			  input:  [${input.joinToString()}]
			  output: [${output.joinToString()}]
			""".trimIndent()
	
	
	@Volatile
	private var outputReady = true
	
	init {
		thread(name = "channel-output-$id") {
			while (!closed) {
				val h = handler
				if (h != null) {
					val change = output.poll(10, TimeUnit.MILLISECONDS)
					if (change != null) {
						if (waitIf(100, 5) { !outputReady }) {
							throw RuntimeException()
						}
						outputReady = false
						when (change) {
							is Change.Data -> {
								val data = change.data
								channelOutputAssistant.execute {
									channelOutputAssistant.schedule(1) { outputReady = true }
									h.handleChannelInput(data)
								}
							}
							Change.Close   -> {
								closed = true
								channelOutputAssistant.execute {
									channelOutputAssistant.schedule(1) { outputReady = true }
									h.handleChannelClose()
								}
							}
						}
					}
				}
			}
		}
	}
	
	override fun send(data: Byte) {
		input.put(Change.Data(ArrayByteBuffer(byteArrayOf(data))))
	}
	
	override fun send(data: ByteArray) {
		input.put(Change.Data(ArrayByteBuffer(data)))
	}
	
	override fun send(data: InputByteBuffer) {
		input.put(Change.Data(ArrayByteBuffer(data)))
	}
	
	override fun close() {
		input.put(Change.Close)
	}
	
	fun bindHandler(handler: ChannelHandler) {
		this.handler = handler
	}
	
	private val fullReceivedData = ArrayByteBuffer()
	
	fun receiveData(expected: InputByteBuffer) {
		val actual: InputByteBuffer
		
		while (true) {
			val change = input.peek()
			if (change == null) {
				Thread.sleep(10)
				if (closed) {
					fail("Unexpected close")
				}
				continue
			}
			assertTrue(change is Change.Data)
			actual = change.data
			break
		}
		
		var i = 0
		while (expected.readable && actual.readable) {
			++i
			val expectedByte = expected.readByte()
			val actualByte = actual.readByte()
			
			if (expectedByte != actualByte) {
				expected.backRead(1)
				actual.backRead(1)
				fail(
					"""
						Invalid received data on channel $id
						  previous: ${fullReceivedData.readToArray().toHexString(' ')}
						  expected: ${expected.readToArray().toHexString(' ')}
						  actual  : ${actual.readToArray().toHexString(' ')}
						  
						""".trimIndent()
				)
			}
			else {
				fullReceivedData.writeByte(actualByte)
			}
		}
		
		if (!actual.readable) {
			input.take()
		}
		
		if (expected.readable) {
			receiveData(expected)
		}
	}
	
	fun receiveDataRead(size: Int, reader: InputByteBuffer.() -> Unit) {
		val actual = ArrayByteBuffer(size)
		
		while (true) {
			val change = input.peek()
			if (change == null) {
				Thread.sleep(10)
				if (closed) {
					fail("Unexpected close")
				}
				continue
			}
			assertTrue(change is Change.Data)
			actual.writeArray(change.data.readToArray(size.coerceAtMost(change.data.readableSize)))
			if (!change.data.readable) {
				input.take()
			}
			if (actual.readableSize == size) {
				break
			}
		}
		
		reader.invoke(actual)
	}
	
	fun receiveClose() {
		val change = input.take()
		assertEquals(Change.Close, change)
		closed = true
	}
	
	fun sendData(data: ArrayByteBuffer) {
		output.put(Change.Data(data))
	}
	
	fun sendClose() {
		output.put(Change.Close)
	}
	
	sealed class Change {
		class Data(val data: InputByteBuffer) : Change() {
			private val raw = data.readToArray()
			
			init {
				data.backRead(raw.size)
			}
			
			override fun toString() = "DATA(${raw.toHexString(' ')})"
		}
		
		object Close : Change() {
			override fun toString() = "CLOSE"
		}
	}
}