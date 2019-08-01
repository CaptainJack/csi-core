package ru.capjack.csi.core.client.stubs

import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionProducer

object DummyConnectionProducer : ConnectionProducer {
	override fun produceConnection(acceptor: ConnectionAcceptor) {
	}
}