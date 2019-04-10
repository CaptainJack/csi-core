package ru.capjack.tool.csi.server

import org.junit.Test
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.wait
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestServer {
	
	private val connectionGateway = StubConnectionGateway()
	
	private val clientAuthorizer = StubClientAuthorizer()
	private val clientAcceptor = StubClientAcceptor()
	
	private val server = Server(
		ScheduledExecutor(Executors.newScheduledThreadPool(4)),
		clientAuthorizer,
		clientAcceptor,
		connectionGateway,
		1
	)
	
	@Test
	fun `Server fast stops on non connections`() {
		val time = now()
		server.stop(1)
		
		assertTrue(time.passedLess(100))
	}
	
	@Test
	fun `Server ignore stop after stop`() {
		server.stop(1)
		server.stop(1)
		
		assertEquals(1, connectionGateway.closeCounter)
	}
	
	@Test
	fun `Server stops on exists connections`() {
		
		val connections = ConcurrentLinkedQueue<StubConnection>()
		
		repeat(3) {
			thread { connectionGateway.produceConnection() }
		}
		
		if (wait(100) { server.statistic.connections != 3 }) {
			fail()
		}
		
		server.stop(1)
		
		if (wait(1000) { server.statistic.connections != 0 }) {
			fail()
		}
		
		for (connection in connections) {
			assertEquals(1, connection.closeCounter)
			assertEqualsBytes(
				"08 00 00 00 01 11",
				connection.output
			)
		}
	}
	
	@Test
	fun `Release connection on server stops`() {
		connectionGateway.produceConnection()
		
		thread { server.stop(1) }
		
		sleep(100)
		
		val connection = connectionGateway.produceConnection()
		
		assertEquals(1, connection.closeCounter)
		assertEqualsBytes("11", connection.output)
		
		if (wait(1000) { server.statistic.connections != 0 }) {
			fail()
		}
	}
	
	@Test
	fun `Hung connection on server stops`() {
		connectionGateway.produceConnection(onSend = { sleep(2000) })
		
		thread { server.stop(1) }
		
		sleep(1100)
		
		assertEquals(1, server.statistic.connections)
		
		sleep(500)
		
		assertEquals(0, server.statistic.connections)
	}
	
	@Test
	fun `Server has non connections after connection closed by inactive`() {
		val connection = connectionGateway.produceConnection()
		
		sleep(2100)
		
		assertEquals(1, connection.closeCounter)
		assertEqualsBytes("12", connection.output)
		
		assertEquals(0, server.statistic.connections)
	}
}
