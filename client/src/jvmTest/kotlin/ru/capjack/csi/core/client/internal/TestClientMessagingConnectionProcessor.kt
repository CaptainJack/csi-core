package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ClientConnectionHandler
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.core.client.DummyConnectionRecoveryHandler
import ru.capjack.csi.core.client._test.FnChannel
import ru.capjack.csi.core.client._test.GLOBAL_ASSISTANT
import ru.capjack.csi.core.client._test.assertEqualsBytes
import ru.capjack.csi.core.client._test.buffer
import ru.capjack.csi.core.client._test.gate
import ru.capjack.csi.core.client._test.write
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingChannel
import ru.capjack.csi.core.common.NothingInternalConnection
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestClientMessagingConnectionProcessor {
	@Test
	fun `When channel closed then handle connection lost and stop pinger`() {
		
		var actualLost = false
		var actualPingerStopped = false
		
		val handler = object : ClientConnectionHandler {
			override fun handleConnectionLost(): ConnectionRecoveryHandler {
				actualLost = true
				return DummyConnectionRecoveryHandler()
			}
			
			override fun handleConnectionCloseTimeout(seconds: Int) {}
			override fun handleConnectionMessage(message: InputByteBuffer) {}
			override fun handleConnectionClose() {}
		}
		
		val assistant = object : DelayableAssistant by GLOBAL_ASSISTANT {
			override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
				return Cancelable { actualPingerStopped = true }
			}
		}
		
		val processor = ClientMessagingConnectionProcessor(
			handler, Messages(), assistant, 1, gate { }, NothingChannel
		)
		
		processor.processChannelClose(NothingInternalConnection)
		
		assertTrue(actualLost)
		assertTrue(actualPingerStopped)
	}
	
	@Test
	fun `When connection recovery then start pinger`() {
		var actualPingerStarts = 0
		
		val assistant = object : DelayableAssistant by GLOBAL_ASSISTANT {
			override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
				++actualPingerStarts
				return Cancelable.DUMMY
			}
		}
		
		val processor = ClientMessagingConnectionProcessor(
			NothingClientConnectionHandler(), Messages(), assistant, 1, gate { }, NothingChannel
		)
		
		processor.processConnectionRecovery(NothingChannel, 0)
		
		assertEquals(2, actualPingerStarts)
	}
	
	@Test
	fun `When connection recovery then stop pinger`() {
		var actualPingerStopped = false
		
		val assistant = object : DelayableAssistant by GLOBAL_ASSISTANT {
			override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
				return Cancelable { actualPingerStopped = true }
			}
		}
		
		val handler = object : ClientConnectionHandler {
			override fun handleConnectionLost(): ConnectionRecoveryHandler = DummyConnectionRecoveryHandler()
			override fun handleConnectionCloseTimeout(seconds: Int) {}
			override fun handleConnectionMessage(message: InputByteBuffer) {}
			override fun handleConnectionClose() {}
		}
		
		val processor = ClientMessagingConnectionProcessor(
			handler, Messages(), assistant, 1, gate { }, NothingChannel
		)
		
		processor.processConnectionClose()
		
		assertTrue(actualPingerStopped)
	}
	
	@Test
	fun `Ping communication`() {
		
		val actualOutput = ArrayByteBuffer()
		
		val channel = FnChannel({
			actualOutput.writeBuffer(this)
		})
		
		val processor = ClientMessagingConnectionProcessor(
			NothingClientConnectionHandler(), Messages(), GLOBAL_ASSISTANT, 1, gate { }, channel
		)
		
		val processInputResult = processor.processChannelInput(channel, buffer("20"))
		
		assertTrue(processInputResult)
		
		waitIf(2100) { actualOutput.readableSize < 2 }
		
		assertEqualsBytes("20 20", actualOutput)
	}
	
	@Test
	fun `When input SERVER_CLOSE_SHUTDOWN then channel closed`() {
		var actualChannelClose = false
		
		val channel = FnChannel(close = { actualChannelClose = true })
		
		val processor = ClientMessagingConnectionProcessor(
			NothingClientConnectionHandler(), Messages(), GLOBAL_ASSISTANT, 1, gate { }, channel
		)
		
		processor.processChannelInput(channel, buffer("54"))
		
		assertTrue(actualChannelClose)
	}
	
	@Test
	fun `When input SERVER_SHUTDOWN_TIMEOUT then handle connection close timeout`() {
		var actualConnectionCloseTimeout = 0
		
		val handler = object : ClientConnectionHandler {
			override fun handleConnectionLost(): ConnectionRecoveryHandler = DummyConnectionRecoveryHandler()
			override fun handleConnectionCloseTimeout(seconds: Int) {
				actualConnectionCloseTimeout = seconds
			}
			override fun handleConnectionMessage(message: InputByteBuffer) {}
			override fun handleConnectionClose() {}
		}
		
		val processor = ClientMessagingConnectionProcessor(
			handler, Messages(), GLOBAL_ASSISTANT, 1, gate { }, NothingChannel
		)
		
		val buffer = buffer { }
		
		buffer.write("40 00 00")
		processor.processChannelInput(NothingChannel, buffer)
		
		
		buffer.write("00 07")
		processor.processChannelInput(NothingChannel, buffer)
		
		assertEquals(7, actualConnectionCloseTimeout)
	}
	
	@Test
	fun `When input message then handle connection message`() {
		val actualMessage = ArrayByteBuffer()
		
		val handler = object : ClientConnectionHandler {
			override fun handleConnectionLost(): ConnectionRecoveryHandler = DummyConnectionRecoveryHandler()
			override fun handleConnectionCloseTimeout(seconds: Int) {}
			override fun handleConnectionMessage(message: InputByteBuffer) {
				actualMessage.writeBuffer(message)
			}
			override fun handleConnectionClose() {}
		}
		
		val processor = ClientMessagingConnectionProcessor(
			handler, Messages(), GLOBAL_ASSISTANT, 1, gate { }, NothingChannel
		)
		
		processor.processChannelInput(NothingChannel, buffer("21  00 00 00 01  00 00 00 01  42"))
		
		assertEqualsBytes("42", actualMessage)
	}
}