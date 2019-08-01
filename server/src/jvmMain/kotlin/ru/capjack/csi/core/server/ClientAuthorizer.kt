package ru.capjack.csi.core.server

import ru.capjack.tool.io.InputByteBuffer

interface ClientAuthorizer {
	fun authorizeClient(authorizationKey: InputByteBuffer): Long?
}

