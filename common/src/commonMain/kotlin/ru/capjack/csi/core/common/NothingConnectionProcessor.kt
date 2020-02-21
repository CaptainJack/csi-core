package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

object NothingConnectionProcessor : ConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {
		throw UnsupportedOperationException()
	}
}