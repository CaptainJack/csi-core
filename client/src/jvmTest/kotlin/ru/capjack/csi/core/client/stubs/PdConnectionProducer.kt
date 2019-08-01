package ru.capjack.csi.core.client.stubs

import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionProducer
import ru.capjack.csi.core.client.utils.threadPoolFactory
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.utils.concurrency.ExecutorImpl
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.fail

class PdConnectionProducer : ConnectionProducer {
	
	private val executor = ExecutorImpl(Executors.newCachedThreadPool(threadPoolFactory("connections")))
	private val counter = AtomicInteger()
	private val next = ConcurrentLinkedQueue<PdConnection>()
	private val connections = ConcurrentLinkedQueue<PdConnection>()
	
	fun next(actions: List<PdConnection.Action>) {
		val connection = PdConnection(counter.incrementAndGet(), executor, actions)
		next.add(connection)
	}
	
	inline fun next(steps: PdConnection.Actions.() -> Unit): PdConnectionProducer {
		next(PdConnection.Actions().apply(steps).list)
		return this
	}
	
	override fun produceConnection(acceptor: ConnectionAcceptor) {
		val connection = next.poll()
		if (connection == null) {
			acceptor.acceptFail()
		}
		else {
			connections.add(connection)
			connection.run(acceptor.acceptSuccess(connection))
		}
	}
	
	fun checkAllConnectionsCompleted(wait: Int) {
		waitIf(wait, check = ::isNotAllConnectionsCompleted)
		checkAllConnectionsCompleted()
	}
	
	fun checkAllConnectionsCompleted() {
		if (isNotAllConnectionsCompleted()) {
			fail("Not all connections completed")
		}
		connections.find { it.failMessage != null }?.let {
			fail(it.failMessage)
		}
	}
	
	private fun isNotAllConnectionsCompleted() = next.isNotEmpty() || !connections.all { it.completed }
}