package ru.capjack.csi.core.common

import ru.capjack.csi.core.Connection

interface InternalConnection : Connection {
	fun accept()
	
	fun recovery(channel: InternalChannel, lastSentMessageId: Int)
	
	fun closeWithMarker(marker: Byte)
	
	fun closeWithMarker(marker: Byte, handler: () -> Unit)
}