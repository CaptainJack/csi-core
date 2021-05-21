package ru.capjack.csi.core.internal

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.pool.ObjectPool

object NothingByteBufferPool : ObjectPool<ByteBuffer> {
	override fun take(): ByteBuffer {
		throw UnsupportedOperationException()
	}
	
	override fun back(instance: ByteBuffer) {
		throw UnsupportedOperationException()
	}
	
	override fun clear() {
		throw UnsupportedOperationException()
	}
}