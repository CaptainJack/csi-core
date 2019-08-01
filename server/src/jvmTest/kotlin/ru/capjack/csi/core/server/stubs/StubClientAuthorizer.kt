package ru.capjack.csi.core.server.stubs

import ru.capjack.csi.core.server.ClientAuthorizer
import ru.capjack.tool.io.InputByteBuffer

class StubClientAuthorizer : ClientAuthorizer {
	override fun authorizeClient(authorizationKey: InputByteBuffer): Long? {
		return if (authorizationKey.readByte() == 0x01.toByte()) authorizationKey.readLong() else null
	}
}