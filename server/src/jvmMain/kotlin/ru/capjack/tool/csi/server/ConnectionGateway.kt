package ru.capjack.tool.csi.server

import ru.capjack.tool.utils.Closeable

interface ConnectionGateway {
	fun open(acceptor: ConnectionAcceptor): Closeable
}

