package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.InputByteBuffer

object EmptyConnectionProcessor : ConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor {
		return NothingConnectionProcessor
	}
	
	override fun processConnectionRecovery(channel: Channel): ConnectionProcessor {
		return NothingConnectionProcessor
	}
	
	override fun processConnectionClose() {
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		return false
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		return NothingConnectionProcessor
	}
}