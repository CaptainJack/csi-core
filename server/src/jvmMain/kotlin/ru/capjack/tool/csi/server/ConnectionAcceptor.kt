package ru.capjack.tool.csi.server

interface ConnectionAcceptor {
	fun acceptConnection(connection: Connection): ConnectionHandler
}

