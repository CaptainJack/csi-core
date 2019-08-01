package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.csi.core.server.Server
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.lang.lefIf

internal class RecoveryConnectionProcessor(
	private val acceptor: RecoveryAcceptor,
	private val statistic: Server.InternalStatistic.Clients
) : ConnectionProcessor {
	
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return buffer.isReadable(8 + 8 + 4).lefIf {
			val client = acceptor.acceptRecovery(buffer.readLong(), buffer.readLong())
			if (client == null) {
				statistic.addRecoveryReject()
				delegate.close(ConnectionCloseReason.RECOVERY_REJECT)
				false
			}
			else {
				statistic.addRecoveryAccept()
				delegate.setProcessor(TransitionConnectionProcessor)
				client.recovery(delegate, buffer.readInt())
				true
			}
		}
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}
