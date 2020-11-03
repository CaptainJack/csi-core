package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.tool.io.getInt

class TestConnectionAuthorizer : ConnectionAuthorizer<Int> {
	override fun authorizeConnection(key: ByteArray): Int? {
		val clientId = key.getInt(0)
		return if (clientId == 0) null else clientId
	}
}