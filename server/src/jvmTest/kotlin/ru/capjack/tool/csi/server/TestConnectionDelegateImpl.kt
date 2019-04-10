package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.server.internal.ConnectionDelegateImpl
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("RemoveRedundantBackticks")
class TestConnectionDelegateImpl {
	
	private val executor = ScheduledExecutor(Executors.newScheduledThreadPool(4))
	private val connection = StubConnection()
	
	@Test
	fun `Send`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(),
			executor,
			1000
		)
		
		connectionDelegate.send(0x01)
		connectionDelegate.send(byteArrayOf(0x02))
		connectionDelegate.send(ByteBuffer { writeByte(0x03.toByte()) })
		
		sleep(10)
		assertEqualsBytes("01 02 03", connection.output)
	}
	
	@Test
	fun `Send after close`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(),
			executor,
			1000
		)
		
		connectionDelegate.close(ConnectionCloseReason.CONSCIOUS)
		
		connectionDelegate.send(0x01)
		connectionDelegate.send(byteArrayOf(0x02))
		connectionDelegate.send(ByteBuffer { writeByte(0x03.toByte()) })
		
		sleep(10)
		assertEquals(1, connection.closeCounter)
		assertEqualsBytes("10", connection.output)
	}
	
	@Test
	fun `Send on input`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(onProcessInput = { c, _ ->
				c.send(0x01)
				c.send(byteArrayOf(0x02))
				c.send(ByteBuffer { writeByte(0x03.toByte()) })
			}),
			executor,
			1000
		)
		
		connectionDelegate.handleInput(ByteBuffer { writeByte(0.toByte()) })
		
		sleep(10)
		
		assertEqualsBytes("01 02 03", connection.output)
	}
	
	@Test
	fun `Close on input`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(onProcessInput = { c, _ ->
				c.close(ConnectionCloseReason.CONSCIOUS)
			}),
			executor,
			1000
		)
		
		thread { connectionDelegate.handleInput(ByteBuffer { writeByte(0.toByte()) }) }
		
		sleep(50)
		
		assertEquals(1, connection.closeCounter)
		assertEqualsBytes("10", connection.output)
	}
	
	@Test
	fun `Handle close`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(),
			executor,
			1000
		)
		
		thread { connectionDelegate.handleClose() }
		
		sleep(10)
		
		assertEquals(1, connection.closeCounter)
		assertEqualsBytes("", connection.output)
	}
	
	
	@Test
	fun `Handle input on input`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(onProcessInput = { c, b ->
				sleep(100)
				c.send(b)
			}),
			executor,
			1000
		)
		
		thread { connectionDelegate.handleInput(ByteBuffer { writeByte(0x01.toByte()) }) }
		sleep(10)
		
		thread { connectionDelegate.handleInput(ByteBuffer { writeByte(0x02.toByte()) }) }
		
		sleep(300)
		
		assertEqualsBytes("01 02", connection.output)
	}
	
	@Test
	fun `Close on input error`() {
		val connectionDelegate = ConnectionDelegateImpl(
			connection,
			StubConnectionReleaser(),
			StubConnectionProcessor(onProcessInput = { _, _ ->
				throw RuntimeException()
			}),
			executor,
			1000
		)
		
		thread { connectionDelegate.handleInput(ByteBuffer { writeByte(0x01.toByte()) }) }
		
		sleep(300)
		
		assertEqualsBytes("17", connection.output)
	}
}

