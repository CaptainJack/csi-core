package ru.capjack.csi.core.client

interface ConnectionProducer {
	fun produceConnection(acceptor: ConnectionAcceptor)
}