package ru.capjack.tool.csi.server.internal

internal interface RecoveryAcceptor {
	fun acceptRecovery(clientId: Long, sessionKey: Long): InternalClient?
	
}
