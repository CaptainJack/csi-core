package ru.capjack.csi.core.client

import ru.capjack.csi.core.Connection

interface ConnectionAcceptor {
	fun acceptConnection(connection: Connection): ConnectionHandler
	
	fun acceptFail(reason: ConnectFailReason)
}