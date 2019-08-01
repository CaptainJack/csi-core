package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer

interface OutgoingMessage {
	val id: Int
	val data: InputByteBuffer
}