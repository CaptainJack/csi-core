package ru.capjack.csi.core.server.stubs

import ru.capjack.csi.core.server.internal.ConnectionDelegate
import ru.capjack.csi.core.server.internal.ConnectionReleaser

internal class DummyConnectionReleaser : ConnectionReleaser {
	override fun releaseConnection(delegate: ConnectionDelegate) {
	}
}
