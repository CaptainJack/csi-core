package ru.capjack.tool.csi.client

interface ConnectionProducer {
	fun produceConnection(acceptor: ConnectionAcceptor)
}