package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.FramedInputByteBuffer

object NothingConnectionProcessor : ConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionClose() {
		throw UnsupportedOperationException()
	}
}