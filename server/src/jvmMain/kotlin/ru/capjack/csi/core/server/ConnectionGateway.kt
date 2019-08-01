package ru.capjack.csi.core.server

import ru.capjack.tool.utils.Closeable

interface ConnectionGateway {
	fun open(acceptor: ConnectionAcceptor): Closeable
}

