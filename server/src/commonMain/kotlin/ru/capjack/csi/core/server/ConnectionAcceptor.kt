package ru.capjack.csi.core.server

import ru.capjack.csi.core.Connection

interface ConnectionAcceptor<I : Any> {
	fun acceptConnection(identity: I, connection: Connection): ConnectionHandler
}

