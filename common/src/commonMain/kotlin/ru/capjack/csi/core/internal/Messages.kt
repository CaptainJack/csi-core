package ru.capjack.csi.core.internal

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.pool.ObjectPool

class Messages(byteBuffers: ObjectPool<ByteBuffer>) {
	val incoming = LastIncomingMessageId()
	val outgoing = OutgoingMessageBuffer(byteBuffers)
}
