package ru.capjack.csi.core.server.internal

internal interface ServerConnectionReleaser<I: Any> {
	fun releaseServerConnection(connection: ServerConnection<I>)
}