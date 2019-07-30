package ru.capjack.tool.csi.core.server

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.csi.core.server.internal.ConnectionDelegate
import ru.capjack.tool.csi.core.server.internal.ConnectionDelegateImpl
import ru.capjack.tool.csi.core.server.internal.ConnectionProcessor
import ru.capjack.tool.csi.core.server.internal.NothingConnectionProcessor
import ru.capjack.tool.csi.core.server.stubs.DummyConnection
import ru.capjack.tool.csi.core.server.stubs.DummyConnectionProcessor
import ru.capjack.tool.csi.core.server.stubs.DummyConnectionReleaser
import ru.capjack.tool.csi.core.server.stubs.DummyScheduledExecutor
import ru.capjack.tool.csi.core.server.stubs.PdConnectionGateway
import ru.capjack.tool.csi.core.server.utils.createServer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.ExecutorImpl
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class TestConnectionDelegate {
	
	@Test
	fun `Connection closed by inactive`() {
		val gateway = PdConnectionGateway()
		val server = createServer(10, 0, gateway)
		
		gateway.produceConnection {
			iData("12")
			iClose()
		}
		
		assertEquals(1, server.statistic.connections)
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `POC setProcessor`() {
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			NothingConnectionProcessor,
			DummyScheduledExecutor(),
			10
		)
		delegate.setProcessor(NothingConnectionProcessor)
	}
	
	@Test
	fun `POC send`() {
		val delegate = ConnectionDelegateImpl(
			object : DummyConnection() {
				override fun send(data: Byte) {
					sleep(100)
				}
			},
			DummyConnectionReleaser(),
			object : ConnectionProcessor {
				override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
					return false
				}
				
				override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
					delegate.send(0)
					thread {
						delegate.send(0)
						delegate.send(byteArrayOf(0))
						delegate.send(ByteBuffer(byteArrayOf(0)))
					}
				}
				
			},
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(4)),
			1000
		)
		delegate.send(0)
		delegate.close()
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ByteBuffer(byteArrayOf(0)))
		
		sleep(200)
	}
	
	@Test
	fun `POC handleClose after closed reason`() {
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			DummyConnectionProcessor(),
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(1)),
			10
		)
		
		delegate.close(ConnectionCloseReason.CLOSE)
		delegate.handleClose()
	}
	
	@Test
	fun `POC close after close`() {
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			object : ConnectionProcessor {
				override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
					thread { delegate.close() }
					sleep(10)
					delegate.close()
					delegate.close()
					return false
				}
				
				override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
				}
				
			},
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(1)),
			10
		)
		
		delegate.handleInput(ByteBuffer(1) { writeByte(1) })
		delegate.close()
		
		sleep(50)
	}
	
	
	@Test
	fun `POC close after close as defer`() {
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			object : ConnectionProcessor {
				override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
					delegate.close()
					return false
				}
				
				override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
				}
			},
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(1)),
			10
		)
		
		delegate.close()
		delegate.handleInput(ByteBuffer(1) { writeByte(1) })
	}
	
	@Test
	fun `POC close reason after close reason`() {
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			object : ConnectionProcessor {
				override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
					thread { delegate.close(ConnectionCloseReason.CLOSE) }
					sleep(10)
					delegate.close(ConnectionCloseReason.CLOSE)
					return false
				}
				
				override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
					sleep(10)
					delegate.close(ConnectionCloseReason.CLOSE)
				}
				
			},
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(1)),
			10
		)
		
		delegate.handleInput(ByteBuffer(byteArrayOf(1)))
		delegate.close(ConnectionCloseReason.CLOSE)
		
		sleep(50)
	}
	
	@Test
	fun `POC checkActivity`() {
		var checkActivity: (() -> Unit)? = null
		
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			DummyConnectionProcessor(),
			object : ScheduledExecutor, ExecutorImpl(Executors.newCachedThreadPool()) {
				override fun repeat(delayMillis: Int, fn: () -> Unit): Cancelable {
					checkActivity = fn
					return Cancelable.DUMMY
				}
				
				override fun schedule(delayMillis: Int, fn: () -> Unit): Cancelable {
					throw UnsupportedOperationException()
				}
			},
			10
		)
		
		checkActivity!!.invoke()
		delegate.close()
		checkActivity!!.invoke()
		checkActivity!!.invoke()
	}
	
	@Test
	fun `POC handleInput after closed concurrent`() {
		val delegate = ConnectionDelegateImpl(
			DummyConnection(),
			DummyConnectionReleaser(),
			object : ConnectionProcessor {
				override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
					sleep(100)
					delegate.close()
					return false
				}
				
				override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
			},
			DummyScheduledExecutor(),
			10
		)
		
		thread {
			delegate.handleInput(ByteBuffer(byteArrayOf(1)))
			delegate.handleInput(ByteBuffer(byteArrayOf(1)))
		}
		sleep(50)
		delegate.handleInput(ByteBuffer(byteArrayOf(1)))
	}
	
	@Test
	fun `POC worker uncaught exception`() {
		val delegate = ConnectionDelegateImpl(
			object : DummyConnection() {
				var t = true
				override fun close() {
					if (t) {
						t = false
						throw RuntimeException()
					}
				}
			},
			DummyConnectionReleaser(),
			DummyConnectionProcessor(),
			ScheduledExecutorImpl(Executors.newScheduledThreadPool(1)),
			10
		)
		
		delegate.close()
		
		sleep(100)
	}
}