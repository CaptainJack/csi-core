package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel

interface InternalChannel : Channel {
	fun useProcessor(processor: ChannelProcessor)
	
	fun useProcessor(processor: ChannelProcessor, activityTimeoutSeconds: Int)
	
	fun closeWithMarker(marker: Byte)
}