package ru.capjack.csi.core.common

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.ObjectPool

class Messages(byteBuffers: ObjectPool<ByteBuffer>) {
	val incoming = LastIncomingMessageId()
	val outgoing = OutgoingMessageBuffer(byteBuffers)
}
