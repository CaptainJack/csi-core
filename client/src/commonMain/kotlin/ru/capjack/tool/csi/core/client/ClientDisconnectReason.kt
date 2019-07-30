package ru.capjack.tool.csi.core.client

enum class ClientDisconnectReason {
	CLOSE,
	SERVER_SHUTDOWN,
	CONCURRENT,
	SERVER_ERROR,
	CLIENT_ERROR,
	PROTOCOL_BROKEN,
	CONNECTION_LOST
}