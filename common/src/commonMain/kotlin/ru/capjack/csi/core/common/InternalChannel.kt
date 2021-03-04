package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel

interface InternalChannel : Channel {
	fun useProcessor(processor: InternalChannelProcessor)
	
	fun useProcessor(processor: InternalChannelProcessor, activityTimeoutSeconds: Int)
	
	fun closeWithMarker(marker: Byte)
}