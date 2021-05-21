package ru.capjack.csi.core.internal

import ru.capjack.tool.io.InputByteBuffer

interface OutgoingMessage {
	val id: Int
	val size: Int
	val data: InputByteBuffer
}