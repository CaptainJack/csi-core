package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.FramedInputByteBuffer

interface ConnectionProcessor {
	fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor
	
	fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor
	
	fun processConnectionClose()
	
	fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean
	
	fun processChannelClose(connection: InternalConnection): ConnectionProcessor
}