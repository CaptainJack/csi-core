package ru.capjack.csi.core.client

import ru.capjack.csi.core.Connection

interface ConnectionAcceptor {
	fun acceptConnection(connection: Connection): ClientConnectionHandler
	
	fun acceptFail(reason: ConnectFailReason)
}