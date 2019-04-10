package ru.capjack.tool.csi.common

import ru.capjack.tool.io.InputByteBuffer

interface OutgoingMessage {
	val id: Int
	val data: InputByteBuffer
}