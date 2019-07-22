package ru.capjack.tool.csi.client

import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.csi.common.ConnectionHandler

interface ConnectionAcceptor {
	fun acceptSuccess(connection: Connection): ConnectionHandler
	
	fun acceptFail()
}