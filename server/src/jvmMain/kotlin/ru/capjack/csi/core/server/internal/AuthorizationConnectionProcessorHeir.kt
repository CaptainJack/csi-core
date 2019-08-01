package ru.capjack.csi.core.server.internal

internal interface AuthorizationConnectionProcessorHeir {
	fun acceptAuthorization(delegate: ConnectionDelegate, clientId: Long): ConnectionProcessor
}