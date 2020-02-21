package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionHandler

internal open class NothingConnectionAcceptor : ConnectionAcceptor {
	override fun acceptConnection(connection: Connection): ConnectionHandler {
		throw UnsupportedOperationException()
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		throw UnsupportedOperationException()
	}
}