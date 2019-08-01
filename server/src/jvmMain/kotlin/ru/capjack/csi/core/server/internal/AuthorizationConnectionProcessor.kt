package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.csi.core.server.ClientAuthorizer
import ru.capjack.csi.core.server.Server
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.lang.alsoIf

internal class AuthorizationConnectionProcessor(
	private val clientAuthorizer: ClientAuthorizer,
	private val heir: AuthorizationConnectionProcessorHeir,
	private val statistic: Server.InternalStatistic.Clients
) : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return alsoIf(buffer.frameView.readable) {
			val clientId = clientAuthorizer.authorizeClient(buffer.frameView)
			if (clientId == null) {
				statistic.addAuthorizationReject()
				delegate.close(ConnectionCloseReason.AUTHORIZATION_REJECT)
			}
			else {
				statistic.addAuthorizationAccept()
				delegate.setProcessor(heir.acceptAuthorization(delegate, clientId))
			}
		}
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}