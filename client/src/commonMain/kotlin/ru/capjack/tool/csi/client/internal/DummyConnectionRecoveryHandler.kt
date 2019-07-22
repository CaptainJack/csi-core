package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.ConnectionRecoveryHandler

class DummyConnectionRecoveryHandler : ConnectionRecoveryHandler {
	override fun handleConnectionRecovered() {}
}