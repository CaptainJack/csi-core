package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.InternalChannelProcessor
import ru.capjack.csi.core.common.InternalChannel

internal interface ConnectionAuthorizationAcceptor<I : Any> {
	fun acceptAuthorization(channel: InternalChannel, identity: I): InternalChannelProcessor
}