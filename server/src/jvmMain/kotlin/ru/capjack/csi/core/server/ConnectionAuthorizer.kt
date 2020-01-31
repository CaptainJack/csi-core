package ru.capjack.csi.core.server

import ru.capjack.tool.io.InputByteBuffer

interface ConnectionAuthorizer<I : Any> {
	fun authorizeConnection(authorizationKey: InputByteBuffer): I?
}

