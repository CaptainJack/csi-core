package ru.capjack.tool.csi.core.server.internal

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.csi.core.ProtocolFlag
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer

internal class ReceptionConnectionProcessor(
	private val heir: ReceptionConnectionProcessorHeir
) : ConnectionProcessor {
	
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return when (buffer.readByte()) {
			ProtocolFlag.AUTHORIZATION -> {
				delegate.setProcessor(heir.acceptAuthorization())
				true
			}
			ProtocolFlag.RECOVERY      -> {
				delegate.setProcessor(heir.acceptRecovery())
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

