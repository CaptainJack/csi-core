package ru.capjack.csi.core.server

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.stubs.PdConnectionGateway
import ru.capjack.csi.core.server.utils.createServer
import ru.capjack.csi.core.server.utils.waitThreads
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.utils.Closeable
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestServer {
	
	@Test(expected = IllegalArgumentException::class)
	fun `Server init fail with negative activity timeout`() {
		createServer(-1, 0, object : ConnectionGateway {
			override fun open(acceptor: ConnectionAcceptor) = Closeable.DUMMY
		})
	}
	
	@Test(expected = IllegalArgumentException::class)
	fun `Server init with negative shutdown timeout`() {
		createServer(1000, -1, object : ConnectionGateway {
			override fun open(acceptor: ConnectionAcceptor) = Closeable.DUMMY
		})
	}
	
	@Test
	fun `Fast stop on non connections`() {
		val server = createServer(1000, 0, object : ConnectionGateway {
			override fun open(acceptor: ConnectionAcceptor) = Closeable.DUMMY
		})
		
		val time = System.currentTimeMillis()
		
		server.stop()
		
		assertTrue(System.currentTimeMillis() - time < 10)
	}
	
	@Test
	fun `One stop on many concurrent stop calls`() {
		val counter = AtomicInteger()
		
		val server = createServer(1000, 0, object : ConnectionGateway, Closeable {
			override fun open(acceptor: ConnectionAcceptor) = this
			override fun close() {
				counter.incrementAndGet()
			}
		})
		
		waitThreads(3) {
			server.stop()
		}
		
		assertEquals(1, counter.get())
	}
	
	
	@Test()
	fun `Stop on silent connections with timeout`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 100, gateway)
		val connections = 3
		
		repeat(connections) {
			gateway.produceConnection {
				iData("08 00 00 00 64 11")
				iClose()
			}
		}
		
		if (waitIf(100) { server.statistic.connections.total != connections }) {
			fail("connections = ${server.statistic.connections}")
		}
		
		server.stop()
		
		if (waitIf(100) { server.statistic.connections.total != 0 }) {
			fail("connections = ${server.statistic.connections}")
		}
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test()
	fun `Stop on silent connections with timeout when they close independently`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 100, gateway)
		val connections = 3
		
		repeat(connections) {
			gateway.produceConnection {
				iData("08 00 00 00 64")
				oClose()
				iClose()
			}
		}
		
		if (waitIf(100) { server.statistic.connections.total != connections }) {
			fail("connections = ${server.statistic.connections}")
		}
		
		server.stop()
		
		if (waitIf(100) { server.statistic.connections.total != 0 }) {
			fail("connections = ${server.statistic.connections}")
		}
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test()
	fun `Stop on silent connections without timeout`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		val connections = 3
		
		repeat(connections) {
			gateway.produceConnection {
				iData("11")
				iClose()
			}
		}
		
		if (waitIf(100) { server.statistic.connections.total != connections }) {
			fail("connections = ${server.statistic.connections}")
		}
		
		server.stop()
		
		if (waitIf(100) { server.statistic.connections.total != 0 }) {
			fail("connections = ${server.statistic.connections}")
		}
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Fast release connection on server stopped`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		server.stop()
		
		gateway.produceConnection {
			iData("11")
			iClose()
		}
		
		sleep(100)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	
	@Test
	fun `Hung connection on server stops`() {
		val server = createServer(1000, 0, object : ConnectionGateway {
			override fun open(acceptor: ConnectionAcceptor): Closeable {
				acceptor.acceptConnection(object : Connection {
					override val id: Any
						get() = 1
					
					override fun send(data: Byte) {}
					
					override fun send(data: ByteArray) {
						sleep(300)
					}
					
					override fun send(data: InputByteBuffer) {}
					
					override fun close() {
					}
					
				})
				return Closeable.DUMMY
			}
		})
		
		
		assertEquals(1, server.statistic.connections.total)
		
		server.stop()
		
		sleep(1000)
		
		assertEquals(0, server.statistic.connections.total)
	}
}
