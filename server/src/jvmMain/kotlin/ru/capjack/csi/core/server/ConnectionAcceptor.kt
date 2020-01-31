package ru.capjack.csi.core.server

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler

interface ConnectionAcceptor<I : Any> {
	fun acceptConnection(identity: I, connection: Connection): ConnectionHandler
}

