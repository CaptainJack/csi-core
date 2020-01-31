package ru.capjack.csi.core.client

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler

interface ChannelAcceptor {
	fun acceptChannel(channel: Channel): ChannelHandler
	
	fun acceptFail()
}