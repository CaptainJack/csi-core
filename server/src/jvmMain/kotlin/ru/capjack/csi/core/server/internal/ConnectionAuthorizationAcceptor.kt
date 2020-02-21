package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannel

internal interface ConnectionAuthorizationAcceptor<I : Any> {
	fun acceptAuthorization(channel: InternalChannel, identity: I): ChannelProcessor
}