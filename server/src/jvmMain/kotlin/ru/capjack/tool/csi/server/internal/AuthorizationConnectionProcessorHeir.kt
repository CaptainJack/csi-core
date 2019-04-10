package ru.capjack.tool.csi.server.internal

internal interface AuthorizationConnectionProcessorHeir {
	fun acceptClient(delegate: ConnectionDelegate, clientId: Long): ConnectionProcessor
}