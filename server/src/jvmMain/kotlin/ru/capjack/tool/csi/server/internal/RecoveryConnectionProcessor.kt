package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
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
				client.recovery(delegate, buffer.readInt())
				true
			}
		}
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}
