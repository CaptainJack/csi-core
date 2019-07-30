package ru.capjack.tool.csi.core.client.stubs

import ru.capjack.tool.csi.core.client.ConnectionAcceptor
import ru.capjack.tool.csi.core.client.ConnectionProducer

object DummyConnectionProducer : ConnectionProducer {
	override fun produceConnection(acceptor: ConnectionAcceptor) {
	}
}