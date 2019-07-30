package ru.capjack.tool.csi.core.server.internal

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.csi.core.server.ClientAuthorizer
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.alsoIf

internal class AuthorizationConnectionProcessor(
	private val clientAuthorizer: ClientAuthorizer,
	private val heir: AuthorizationConnectionProcessorHeir
) : ConnectionProcessor {
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return alsoIf(buffer.framedView.readable) {
			val clientId = clientAuthorizer.authorizeClient(buffer.framedView)
			if (clientId == null) {
				delegate.close(ConnectionCloseReason.AUTHORIZATION_REJECT)
			}
			else {
				delegate.setProcessor(heir.acceptClient(delegate, clientId))
			}
		}
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}