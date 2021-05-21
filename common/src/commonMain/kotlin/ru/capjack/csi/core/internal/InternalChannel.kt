package ru.capjack.csi.core.internal

import ru.capjack.csi.core.Channel

interface InternalChannel : Channel {
	fun useProcessor(processor: InternalChannelProcessor)
	
	fun useProcessor(processor: InternalChannelProcessor, activityTimeoutSeconds: Int)
	
	fun closeWithMarker(marker: Byte)
}