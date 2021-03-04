package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.InternalConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.InputByteBuffer

object EmptyConnectionProcessor : InternalConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		return NothingConnectionProcessor
	}
	
	override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		return NothingConnectionProcessor
	}
	
	override fun processConnectionClose() {
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		return false
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
		return NothingConnectionProcessor
	}
}