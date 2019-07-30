package ru.capjack.tool.csi.core.server

import ru.capjack.tool.csi.core.Connection
import ru.capjack.tool.csi.core.ConnectionHandler

interface ConnectionAcceptor {
	fun acceptConnection(connection: Connection): ConnectionHandler
}

