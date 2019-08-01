package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.server.ServerStatistic

internal data class SnapshotServerStatistic(
	override val connections: Connections,
	override val clients: Clients
) : ServerStatistic {
	
	override fun snap(): ServerStatistic = this
	
	override fun toString(): String {
		return "connections: $connections, clients: $clients"
	}
	
	data class Connections(
		override val total: Int,
		override val accept: Int,
		override val release: Int
	) : ServerStatistic.Connections {
		override fun toString(): String {
			return "$total (a: $accept, r: $release)"
		}
	}
	
	data class Clients(
		override val total: Int,
		override val authorizationAccept: Int,
		override val authorizationReject: Int,
		override val recoveryAccept: Int,
		override val recoveryReject: Int,
		override val messageInput: Int,
		override val messageOutput: Int,
		override val messageRecovery: Int
	) : ServerStatistic.Clients {
		override fun toString(): String {
			return "$total (" +
				"aa: $authorizationAccept, " +
				"ar: $authorizationReject, " +
				"ra: $recoveryAccept, " +
				"rr: $recoveryReject, " +
				"mi: $messageInput, " +
				"mo: $messageOutput, " +
				"mr: $messageRecovery" +
				")"
		}
	}
}
