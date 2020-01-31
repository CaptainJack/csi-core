package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ClientConnectionHandler
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor

internal open class NothingConnectionAcceptor : ConnectionAcceptor {
	override fun acceptConnection(connection: Connection): ClientConnectionHandler {
		throw UnsupportedOperationException()
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		throw UnsupportedOperationException()
	}
}