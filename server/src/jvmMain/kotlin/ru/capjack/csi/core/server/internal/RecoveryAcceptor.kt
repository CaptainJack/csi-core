package ru.capjack.csi.core.server.internal

internal interface RecoveryAcceptor {
	fun acceptRecovery(clientId: Long, sessionKey: Long): InternalClient?
}
