package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.FramedInputByteBuffer

object EmptyConnectionProcessor : ConnectionProcessor {
	override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		return NothingConnectionProcessor
	}
	
	override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		return NothingConnectionProcessor
	}
	
	override fun processConnectionClose() {
	}
	
	override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		return false
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		return NothingConnectionProcessor
	}
}