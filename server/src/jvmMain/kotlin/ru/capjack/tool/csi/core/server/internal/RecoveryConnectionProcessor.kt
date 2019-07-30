package ru.capjack.tool.csi.core.server.internal

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.lang.lefIf

internal class RecoveryConnectionProcessor(
	private val acceptor: RecoveryAcceptor
) : ConnectionProcessor {
	
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return buffer.isReadable(8 + 8 + 4).lefIf {
			val client = acceptor.acceptRecovery(buffer.readLong(), buffer.readLong())
			if (client == null) {
				delegate.close(ConnectionCloseReason.RECOVERY_REJECT)
				false
			}
			else {
				delegate.setProcessor(TransitionConnectionProcessor)
				client.recovery(delegate, buffer.readInt())
				true
			}
		}
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}
