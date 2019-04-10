package ru.capjack.tool.csi.server

import ru.capjack.tool.utils.Closeable
import java.util.concurrent.atomic.AtomicInteger

class StubConnectionGateway : ConnectionGateway, Closeable {
	private val _closeCounter = AtomicInteger()
	val closeCounter: Int
		get() = _closeCounter.get()
	
	private lateinit var acceptor: ConnectionAcceptor
	
	override fun openGate(acceptor: ConnectionAcceptor): Closeable {
		this.acceptor = acceptor
		return this
	}
	
	override fun close() {
		_closeCounter.getAndIncrement()
	}
	
	fun produceConnection(
		onSend: () -> Unit = {}
	): StubConnection {
		return StubConnection(onSend).also {
			it.handler = acceptor.acceptConnection(it)
		}
	}
}

