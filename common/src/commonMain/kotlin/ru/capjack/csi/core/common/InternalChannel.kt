package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel

interface InternalChannel : Channel {
	fun useProcessor(processor: ChannelProcessor)
	
	fun closeWithMarker(marker: Byte)
}