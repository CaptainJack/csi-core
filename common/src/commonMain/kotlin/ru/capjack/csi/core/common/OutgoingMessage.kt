package ru.capjack.csi.core.common

import ru.capjack.tool.io.InputByteBuffer

interface OutgoingMessage {
	val id: Int
	val size: Int
	val data: InputByteBuffer
}