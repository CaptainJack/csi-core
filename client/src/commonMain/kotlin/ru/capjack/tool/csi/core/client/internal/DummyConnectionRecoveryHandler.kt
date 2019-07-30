package ru.capjack.tool.csi.core.client.internal

import ru.capjack.tool.csi.core.client.ConnectionRecoveryHandler

class DummyConnectionRecoveryHandler : ConnectionRecoveryHandler {
	override fun handleConnectionRecovered() {}
}