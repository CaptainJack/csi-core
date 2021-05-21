package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.InternalChannel

internal interface ConnectionAuthorizationAcceptor<I : Any> {
	fun acceptAuthorization(channel: InternalChannel, identity: I): InternalChannelProcessor
}