package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ConnectionRecoveryHandler

class DummyConnectionRecoveryHandler : ConnectionRecoveryHandler {
	override fun handleConnectionRecovered() {}
}