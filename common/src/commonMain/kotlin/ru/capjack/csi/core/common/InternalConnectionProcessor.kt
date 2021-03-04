package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

interface InternalConnectionProcessor {
	fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor
	
	fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor
	
	fun processConnectionClose()
	
	fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean
	
	fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor
}