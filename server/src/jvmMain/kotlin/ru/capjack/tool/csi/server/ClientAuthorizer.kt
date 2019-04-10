package ru.capjack.tool.csi.server

import ru.capjack.tool.io.InputByteBuffer

interface ClientAuthorizer {
	fun authorizeClient(authorizationKey: InputByteBuffer): Long?
}

