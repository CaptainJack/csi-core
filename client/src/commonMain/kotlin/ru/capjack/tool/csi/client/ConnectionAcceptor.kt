package ru.capjack.tool.csi.client

interface ConnectionAcceptor {
	fun acceptSuccess(connection: Connection): ConnectionHandler
	
	fun acceptFail()
}