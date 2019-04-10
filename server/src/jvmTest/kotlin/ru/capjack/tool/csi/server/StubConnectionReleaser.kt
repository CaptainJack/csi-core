package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.server.internal.ConnectionReleaser
import ru.capjack.tool.csi.server.internal.ConnectionDelegate

internal class StubConnectionReleaser(
	private val onReleaseConnection: () -> Unit = {}
) : ConnectionReleaser {
	override fun releaseConnection(delegate: ConnectionDelegate) {
		onReleaseConnection()
	}
}