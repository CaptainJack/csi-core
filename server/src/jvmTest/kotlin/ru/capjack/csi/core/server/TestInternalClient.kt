package ru.capjack.csi.core.server

import ru.capjack.csi.core.server.internal.DummyConnectionDelegate
import ru.capjack.csi.core.server.internal.InternalClientImpl
import ru.capjack.csi.core.server.stubs.DummyClientAcceptor
import ru.capjack.csi.core.server.stubs.DummyScheduledExecutor
import ru.capjack.csi.core.server.stubs.PdConnectionGateway
import ru.capjack.csi.core.server.stubs.SlowlyConnectionDelegate
import ru.capjack.csi.core.server.stubs.StubInternalStatistic
import ru.capjack.csi.core.server.utils.addDisconnectHandler
import ru.capjack.csi.core.server.utils.createServer
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.FramedArrayByteBuffer
import ru.capjack.tool.lang.waitIf
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
			ScheduledExecutorImpl(Executors.newSingleThreadScheduledExecutor()),
			10,
			StubInternalStatistic.Clients()
		)
		
		client.disconnect()
		
		var invoked = false
		client.addDisconnectHandler {
			invoked = true
		}
		
		waitIf(100) { invoked }
		
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
		assertEquals(0, server.statistic.clients.total)
	}
	
	@Test
	fun `POC processClose`() {
		
		val delegate = SlowlyConnectionDelegate(sleepOnSend = 150)
		
		val client = InternalClientImpl(
			1,
			delegate,
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(2)),
			1000,
			StubInternalStatistic.Clients()
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
			10,
			StubInternalStatistic.Clients()
		)
		
		client.processInput(DummyConnectionDelegate, FramedArrayByteBuffer(1).apply { writeByte(1) })
	}
	
	@Test
	fun `POC AcceptationProcessor processRecovery`() {
		val client = InternalClientImpl(
			1,
			DummyConnectionDelegate,
			DummyScheduledExecutor(),
			10,
			StubInternalStatistic.Clients()
		)
		
		client.recovery(DummyConnectionDelegate, 0)
	}
	
	@Test
	fun `POC RecoveryProcessor`() {
		val client = InternalClientImpl(
			1,
			DummyConnectionDelegate,
			DummyScheduledExecutor(),
			10,
			StubInternalStatistic.Clients()
		)
		
		client.accept(DummyClientAcceptor)
		
		client.processClose(DummyConnectionDelegate, true)
		
		client.processInput(DummyConnectionDelegate, FramedArrayByteBuffer(1).apply { writeByte(1) })
		client.processClose(DummyConnectionDelegate, true)
	}
	
	@Test
	fun `POC accept and disconnect`() {
		val delegate = SlowlyConnectionDelegate(sleepOnClose = 100)
		
		val client = InternalClientImpl(
			1,
			delegate,
			DummyScheduledExecutor(),
			10,
			StubInternalStatistic.Clients()
		)
		
		thread {
			client.processInput(delegate, FramedArrayByteBuffer(1).apply { writeByte(1) })
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
			10,
			StubInternalStatistic.Clients()
		)
		
		thread {
			client.processInput(delegate, FramedArrayByteBuffer(1).apply { writeByte(1) })
		}
		sleep(10)
		
		client.disconnect()
		client.sendMessage(1)
		client.sendMessage(byteArrayOf(1))
		client.sendMessage(ArrayByteBuffer(byteArrayOf(1)))
		
		sleep(200)
		
		client.sendMessage(1)
		client.sendMessage(byteArrayOf(1))
		client.sendMessage(ArrayByteBuffer(byteArrayOf(1)))
	}
}
