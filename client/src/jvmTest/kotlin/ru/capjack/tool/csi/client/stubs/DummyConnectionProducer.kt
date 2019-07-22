package ru.capjack.tool.csi.client.stubs

import ru.capjack.tool.csi.client.ConnectionAcceptor
import ru.capjack.tool.csi.client.ConnectionProducer

object DummyConnectionProducer : ConnectionProducer {
	override fun produceConnection(acceptor: ConnectionAcceptor) {
	}
}