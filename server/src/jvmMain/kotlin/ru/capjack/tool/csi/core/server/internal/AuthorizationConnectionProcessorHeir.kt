package ru.capjack.tool.csi.core.server.internal

internal interface AuthorizationConnectionProcessorHeir {
	fun acceptClient(delegate: ConnectionDelegate, clientId: Long): ConnectionProcessor
}