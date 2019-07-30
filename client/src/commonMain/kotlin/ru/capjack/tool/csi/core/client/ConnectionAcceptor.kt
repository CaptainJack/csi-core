package ru.capjack.tool.csi.core.client

import ru.capjack.tool.csi.core.Connection
import ru.capjack.tool.csi.core.ConnectionHandler

interface ConnectionAcceptor {
	fun acceptSuccess(connection: Connection): ConnectionHandler
	
	fun acceptFail()
}