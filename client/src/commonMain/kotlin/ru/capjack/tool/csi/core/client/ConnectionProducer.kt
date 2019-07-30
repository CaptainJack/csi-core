package ru.capjack.tool.csi.core.client

interface ConnectionProducer {
	fun produceConnection(acceptor: ConnectionAcceptor)
}