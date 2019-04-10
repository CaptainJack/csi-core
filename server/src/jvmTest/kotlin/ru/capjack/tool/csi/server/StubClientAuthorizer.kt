package ru.capjack.tool.csi.server

import ru.capjack.tool.io.InputByteBuffer
import java.util.concurrent.atomic.AtomicInteger

class StubClientAuthorizer : ClientAuthorizer {
	private val _authorizeCounter = AtomicInteger()
	val authorizeCounter
		get() = _authorizeCounter.get()
	
	override fun authorizeClient(authorizationKey: InputByteBuffer): Long? {
		_authorizeCounter.getAndIncrement()
		return if (authorizationKey.readByte() == 0x01.toByte()) authorizationKey.readLong() else null
	}
}