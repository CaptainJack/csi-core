package ru.capjack.csi.core.server

interface ServerStatistic {
	val connections: Connections
	val clients: Clients
	
	fun snap(): ServerStatistic
	
	interface Connections {
		val total: Int
		val accept: Int
		val release: Int
	}
	
	interface Clients {
		val total: Int
		val authorizationAccept: Int
		val authorizationReject: Int
		val recoveryAccept: Int
		val recoveryReject: Int
		val messageInput: Int
		val messageOutput: Int
		val messageRecovery: Int
	}
}
