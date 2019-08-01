package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.csi.core.ProtocolFlag
import ru.capjack.tool.io.FramedInputByteBuffer

internal class ReceptionConnectionProcessor(
	private val heir: ReceptionConnectionProcessorHeir
) : ConnectionProcessor {
	
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return when (buffer.readByte()) {
			ProtocolFlag.AUTHORIZATION -> {
				delegate.setProcessor(heir.proceedAuthorization())
				true
			}
			ProtocolFlag.RECOVERY      -> {
				delegate.setProcessor(heir.proceedRecovery())
				true
			}
			else                                                -> {
				delegate.close(ConnectionCloseReason.PROTOCOL_BROKEN)
				false
			}
		}
	}
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {}
}

