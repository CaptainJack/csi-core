package ru.capjack.tool.csi.core.server.stubs

import ru.capjack.tool.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.csi.core.server.ConnectionGateway
import ru.capjack.tool.csi.core.server.utils.threadPoolFactory
import ru.capjack.tool.utils.Closeable
import ru.capjack.tool.utils.concurrency.ExecutorImpl
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.fail

class PdConnectionGateway : ConnectionGateway {
	private lateinit var acceptor: ConnectionAcceptor
	
	private val executor = ExecutorImpl(Executors.newCachedThreadPool(threadPoolFactory("connections")))
	private val counter = AtomicInteger()
	private val connections = ConcurrentLinkedQueue<PdConnection>()
	
	override fun open(acceptor: ConnectionAcceptor): Closeable {
		this.acceptor = acceptor
		return Closeable.DUMMY
	}
	
	fun produceConnection(actions: List<PdConnection.Action>) {
		val connection = PdConnection(counter.incrementAndGet(), executor, actions)
		connections.add(connection)
		
		connection.run(acceptor.acceptConnection(connection))
	}
	
	inline fun produceConnection(steps: PdConnection.Actions.() -> Unit) {
		produceConnection(PdConnection.Actions().apply(steps).list)
	}
	
	fun checkAllConnectionsCompleted() {
		if (!connections.all { it.completed }) {
			fail("Not all connections completed")
		}
		connections.find { it.failMessage != null }?.let {
			fail(it.failMessage)
		}
	}
}

