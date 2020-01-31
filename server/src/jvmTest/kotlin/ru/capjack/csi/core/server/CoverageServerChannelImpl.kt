package ru.capjack.csi.core.server

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.server._test.EmptyChannel
import ru.capjack.csi.core.server._test.EmptyChannelProcessor
import ru.capjack.csi.core.server._test.EmptyServerChannelReleaser
import ru.capjack.csi.core.server.internal.ServerChannel
import ru.capjack.csi.core.server.internal.ServerChannelImpl
import ru.capjack.csi.core.server.internal.ServerChannelReleaser
import ru.capjack.csi.core.server._test.assistant
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.EMPTY_FUNCTION_0
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import java.lang.Thread.sleep
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class CoverageServerChannelImpl {
	
	@Test
	fun `Coverage checkActivity`() {
		var opened = true
		var callback: () -> Unit = EMPTY_FUNCTION_0
		
		val channel = ServerChannelImpl(
			EmptyChannel,
			EmptyChannelProcessor,
			object : DelayableAssistant {
				override fun execute(code: () -> Unit) {}
				
				override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
					callback = code
					return Cancelable.DUMMY
				}
				
				override fun schedule(delayMillis: Int, code: () -> Unit): Cancelable = Cancelable.DUMMY
				
				override fun charge(code: () -> Unit): Cancelable = Cancelable.DUMMY
				
			},
			1,
			object : ServerChannelReleaser {
				override fun releaseServerChannel(channel: ServerChannel) {
					opened = false
				}
			}
		)
		
		channel.close()
		callback.invoke()
		
		waitIf(1000) { opened }
		
		callback.invoke()
	}
	
	@Test
	fun `Coverage close`() {
		var opened = true
		val channel = ServerChannelImpl(object : Channel {
			override val id: Any = 0
			
			override fun send(data: Byte) {}
			
			override fun send(data: ByteArray) {}
			
			override fun send(data: InputByteBuffer) {
				sleep(100)
				data.skipRead()
			}
			
			override fun close() {
				opened = false
			}
			
		},
			EmptyChannelProcessor,
			assistant(), 1,
			EmptyServerChannelReleaser
		)
		
		channel.closeWithMarker(0)
		channel.close()
		channel.handleChannelInput(ArrayByteBuffer())
		channel.handleChannelClose()
		
		waitIf(1000) { opened }
		
		channel.close()
		channel.handleChannelClose()
		channel.handleChannelInput(ArrayByteBuffer())
	}
	
	@Test
	fun `Coverage output buffer must be read in full`() {
		val channel = ServerChannelImpl(object : Channel {
			override val id: Any = 0
			
			override fun send(data: Byte) {}
			
			override fun send(data: ByteArray) {}
			
			override fun send(data: InputByteBuffer) {}
			
			override fun close() {}
			
		},
			EmptyChannelProcessor,
			assistant(), 1,
			EmptyServerChannelReleaser
		)
		
		channel.closeWithMarker(0)
	}
	
	@Test(expected = IllegalStateException::class)
	fun `Illegal useProcessor`() {
		var opened = true
		val channel = ServerChannelImpl(
			EmptyChannel,
			EmptyChannelProcessor,
			assistant(), 1, object : ServerChannelReleaser {
			override fun releaseServerChannel(channel: ServerChannel) {
				assertFailsWith<IllegalStateException> {
					channel.useProcessor(EmptyChannelProcessor)
				}
				sleep(10)
				opened = false
			}
		})
		
		channel.close()
		
		waitIf(1000) { opened }
		
		channel.useProcessor(EmptyChannelProcessor)
	}
	
	
	@Test
	fun `Send byte after close`() {
		var opened = true
		val channel = ServerChannelImpl(
			EmptyChannel,
			EmptyChannelProcessor,
			assistant(), 1, object : ServerChannelReleaser {
			override fun releaseServerChannel(channel: ServerChannel) {
				sleep(10)
				opened = false
			}
		})
		
		channel.send(0)
		
		channel.close()
		channel.send(1)
		
		waitIf(1000) { opened }
		
		channel.send(2)
	}
	
	@Test
	fun `Send array after close`() {
		var opened = true
		val channel = ServerChannelImpl(
			EmptyChannel,
			EmptyChannelProcessor,
			assistant(), 1, object : ServerChannelReleaser {
			override fun releaseServerChannel(channel: ServerChannel) {
				sleep(10)
				opened = false
			}
		})
		
		channel.close()
		channel.send(byteArrayOf(1))
		
		waitIf(1000) { opened }
		
		channel.send(byteArrayOf(2))
	}
	
	@Test
	fun `Send buffer after close`() {
		var opened = true
		val buffer = ArrayByteBuffer { writeInt(1) }
		val channel = ServerChannelImpl(
			EmptyChannel,
			EmptyChannelProcessor,
			assistant(), 1, object : ServerChannelReleaser {
			override fun releaseServerChannel(channel: ServerChannel) {
				sleep(10)
				opened = false
			}
		})
		
		channel.close()
		channel.send(buffer)
		
		waitIf(1000) { opened }
		
		assertFalse(buffer.readable)
		
		buffer.writeInt(2)
		channel.send(buffer)
		
		assertFalse(buffer.readable)
	}
}