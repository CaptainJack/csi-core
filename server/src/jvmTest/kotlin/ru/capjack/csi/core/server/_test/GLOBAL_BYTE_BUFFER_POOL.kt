package ru.capjack.csi.core.server._test

import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.pool.ArrayObjectPool
import ru.capjack.tool.utils.pool.ObjectAllocator

val GLOBAL_BYTE_BUFFER_POOL = ArrayObjectPool(64, object : ObjectAllocator<ByteBuffer> {
	override fun produceInstance(): ByteBuffer {
		return ArrayByteBuffer()
	}
	
	override fun clearInstance(instance: ByteBuffer) {
		instance.clear()
	}
	
	override fun disposeInstance(instance: ByteBuffer) {
		instance.clear()
	}
})