package ru.capjack.csi.core.common

import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger

interface InternalConnection : Connection {
	val logger: Logger
	val messages: Messages
	
	fun accept()
	
	fun recovery(channel: InternalChannel, lastSentMessageId: Int)
	
	fun closeWithMarker(marker: Byte)
	
	fun closeWithMarker(marker: Byte, handler: () -> Unit)
}