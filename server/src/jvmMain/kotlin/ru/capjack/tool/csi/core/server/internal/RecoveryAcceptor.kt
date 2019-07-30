package ru.capjack.tool.csi.core.server.internal

internal interface RecoveryAcceptor {
	fun acceptRecovery(clientId: Long, sessionKey: Long): InternalClient?
}
