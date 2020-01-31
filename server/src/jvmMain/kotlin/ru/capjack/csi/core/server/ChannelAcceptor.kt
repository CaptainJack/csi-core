package ru.capjack.csi.core.server

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler

interface ChannelAcceptor {
	fun acceptChannel(channel: Channel): ChannelHandler
}

