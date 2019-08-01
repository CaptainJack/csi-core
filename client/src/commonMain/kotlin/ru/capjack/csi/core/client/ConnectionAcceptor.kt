package ru.capjack.csi.core.client

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler

interface ConnectionAcceptor {
	fun acceptSuccess(connection: Connection): ConnectionHandler
	
	fun acceptFail()
}