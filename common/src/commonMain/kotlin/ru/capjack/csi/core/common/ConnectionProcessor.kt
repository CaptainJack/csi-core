package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

interface ConnectionProcessor {
	fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor
	
	fun processConnectionRecovery(channel: Channel): ConnectionProcessor
	
	fun processConnectionClose()
	
	fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean
	
	fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor
}