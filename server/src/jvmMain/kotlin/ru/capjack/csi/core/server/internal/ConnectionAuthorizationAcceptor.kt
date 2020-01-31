package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.ChannelProcessor

internal interface ConnectionAuthorizationAcceptor<I : Any> {
	fun acceptAuthorization(channel: InternalChannel, identity: I): ChannelProcessor
}