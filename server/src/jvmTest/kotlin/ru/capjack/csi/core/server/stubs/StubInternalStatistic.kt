package ru.capjack.csi.core.server.stubs

import ru.capjack.csi.core.server.Server
import ru.capjack.csi.core.server.ServerStatistic

internal class StubInternalStatistic : Server.InternalStatistic {
	override val connections: Server.InternalStatistic.Connections = Connections()
	override val clients: Server.InternalStatistic.Clients = Clients()
	
	override fun snap(): ServerStatistic = this
	
	class Connections : Server.InternalStatistic.Connections {
		override val total: Int = 0
		override val accept: Int = 0
		override val release: Int = 0
		
		override fun addAccept() {}
		override fun addRelease() {}
	}
	
	class Clients : Server.InternalStatistic.Clients {
		override val total: Int = 0
		override val authorizationAccept: Int = 0
		override val authorizationReject: Int = 0
		override val recoveryAccept: Int = 0
		override val recoveryReject: Int = 0
		override val messageInput: Int = 0
		override val messageOutput: Int = 0
		override val messageRecovery: Int = 0
		
		override fun addAuthorizationAccept() {}
		override fun addAuthorizationReject() {}
		override fun addRecoveryAccept() {}
		override fun addRecoveryReject() {}
		override fun addMessageInput() {}
		override fun addMessageOutput() {}
		override fun addMessageRecovery() {}
	}
}