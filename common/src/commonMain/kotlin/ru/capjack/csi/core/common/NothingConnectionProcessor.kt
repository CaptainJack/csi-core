package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

object NothingConnectionProcessor : InternalConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {
		throw UnsupportedOperationException()
	}
}