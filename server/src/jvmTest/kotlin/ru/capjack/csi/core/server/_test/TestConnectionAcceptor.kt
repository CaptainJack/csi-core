package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.server.ConnectionAcceptor

class TestConnectionAcceptor : ConnectionAcceptor<Int> {
	override fun acceptConnection(identity: Int, connection: Connection): ConnectionHandler {
		return TestConnectionHandler(connection)
	}
}