package ru.capjack.tool.csi.server.stubs

import ru.capjack.tool.csi.server.internal.ConnectionDelegate
import ru.capjack.tool.csi.server.internal.ConnectionReleaser

internal class DummyConnectionReleaser : ConnectionReleaser {
	override fun releaseConnection(delegate: ConnectionDelegate) {
	}
}
