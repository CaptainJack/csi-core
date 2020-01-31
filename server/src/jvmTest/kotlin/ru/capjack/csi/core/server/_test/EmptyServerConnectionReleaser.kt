package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.server.internal.ServerConnection
import ru.capjack.csi.core.server.internal.ServerConnectionReleaser

internal object EmptyServerConnectionReleaser : ServerConnectionReleaser<Int> {
	override fun releaseServerConnection(connection: ServerConnection<Int>) {}
}

