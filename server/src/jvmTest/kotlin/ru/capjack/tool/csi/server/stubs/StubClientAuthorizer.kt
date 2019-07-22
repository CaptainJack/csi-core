package ru.capjack.tool.csi.server.stubs

import ru.capjack.tool.csi.server.ClientAuthorizer
import ru.capjack.tool.io.InputByteBuffer

class StubClientAuthorizer : ClientAuthorizer {
	override fun authorizeClient(authorizationKey: InputByteBuffer): Long? {
		return if (authorizationKey.readByte() == 0x01.toByte()) authorizationKey.readLong() else null
	}
}