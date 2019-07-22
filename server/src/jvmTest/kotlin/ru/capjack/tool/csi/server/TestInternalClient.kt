package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.server.internal.DummyConnectionDelegate
import ru.capjack.tool.csi.server.internal.InternalClientImpl
import ru.capjack.tool.csi.server.stubs.DummyClientAcceptor
import ru.capjack.tool.csi.server.stubs.DummyScheduledExecutor
import ru.capjack.tool.csi.server.stubs.PdConnectionGateway
import ru.capjack.tool.csi.server.stubs.SlowlyConnectionDelegate
import ru.capjack.tool.csi.server.utils.addDisconnectHandler
import ru.capjack.tool.csi.server.utils.createServer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedByteBuffer
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestInternalClient {
	@Test
	fun `Disconnect handler added after disconnect invoked anyway`() {
		val client = InternalClientImpl(
			1,
			DummyConnectionDelegate,
			DummyScheduledExecutor(),
			10
		)
		
		client.disconnect()
		
		var invoked = false
		client.addDisconnectHandler {
			invoked = true
		}
		
		assertTrue(invoked)
	}
	
	@Test
	fun `Disconnect with inactivity`() {
		val gateway = PdConnectionGateway()
		val server = createServer(100, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			oClose()
			iClose()
		}
		
		sleep(300)
		
		gateway.checkAllConnectionsCompleted()
		assertEquals(0, server.statistic.clients)
	}
	
	@Test
	fun `POC processClose`() {
		
		val delegate = SlowlyConnectionDelegate(sleepOnSend = 150)
		
		val client = InternalClientImpl(
			1,
			delegate,
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(2)),
			1000
		)
		
		thread {
			client.sendMessage(1)
			client.processClose(delegate, false)
		}
		
		sleep(50)
		
		client.processClose(DummyConnectionDelegate, false)
		client.processClose(delegate, false)
		client.processClose(delegate, false)
		
		sleep(200)
	}
	
	@Test
	fun `POC AcceptationProcessor processInput`() {
		val client = InternalClientImpl(
			1,
			DummyConnectionDelegate,
			DummyScheduledExecutor(),
			10
		)
		
		client.processInput(DummyConnectionDelegate, FramedByteBuffer(1).apply { writeByte(1) })
	}
	
	@Test
	fun `POC AcceptationProcessor processRecovery`() {
		val client = InternalClientImpl(
			1,
			DummyConnectionDelegate,
			DummyScheduledExecutor(),
			10
		)
		
		client.recovery(DummyConnectionDelegate, 0)
	}
	
	@Test
	fun `POC RecoveryProcessor`() {
		val client = InternalClientImpl(
			1,
			DummyConnectionDelegate,
			DummyScheduledExecutor(),
			10
		)
		
		client.accept(DummyClientAcceptor)
		
		client.processClose(DummyConnectionDelegate, true)
		
		client.processInput(DummyConnectionDelegate, FramedByteBuffer(1).apply { writeByte(1) })
		client.processClose(DummyConnectionDelegate, true)
	}
	
	@Test
	fun `POC accept and disconnect`() {
		val delegate = SlowlyConnectionDelegate(sleepOnClose = 100)
		
		val client = InternalClientImpl(
			1,
			delegate,
			DummyScheduledExecutor(),
			10
		)
		
		thread {
			client.processInput(delegate, FramedByteBuffer(1).apply { writeByte(1) })
		}
		sleep(10)
		
		client.disconnect()
		client.disconnect()
		client.disconnectOfConcurrent()
		client.accept(DummyClientAcceptor)
		
		sleep(200)
		
		client.disconnect()
		client.disconnectOfConcurrent()
	}
	
	@Test
	fun `POC sendMessage`() {
		val delegate = SlowlyConnectionDelegate(sleepOnClose = 100)
		
		val client = InternalClientImpl(
			1,
			delegate,
			DummyScheduledExecutor(),
			10
		)
		
		thread {
			client.processInput(delegate, FramedByteBuffer(1).apply { writeByte(1) })
		}
		sleep(10)
		
		client.disconnect()
		client.sendMessage(1)
		client.sendMessage(byteArrayOf(1))
		client.sendMessage(ByteBuffer(byteArrayOf(1)))
		
		sleep(200)
		
		client.sendMessage(1)
		client.sendMessage(byteArrayOf(1))
		client.sendMessage(ByteBuffer(byteArrayOf(1)))
	}
}
