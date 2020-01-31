package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.tool.io.InputByteBuffer

class TestConnectionAuthorizer : ConnectionAuthorizer<Int> {
	override fun authorizeConnection(authorizationKey: InputByteBuffer): Int? {
		val clientId = authorizationKey.readInt()
		return if (clientId == 0) null else clientId
	}
}