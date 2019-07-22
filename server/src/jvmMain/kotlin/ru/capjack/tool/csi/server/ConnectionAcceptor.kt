package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.csi.common.ConnectionHandler

interface ConnectionAcceptor {
	fun acceptConnection(connection: Connection): ConnectionHandler
}

