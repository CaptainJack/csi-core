package ru.capjack.csi.core.server.internal

internal interface ConnectionRecoveryAcceptor {
	fun acceptRecovery(connectionId: Long): ServerConnection<*>?
}
