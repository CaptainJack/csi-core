package ru.capjack.csi.core.common

import ru.capjack.csi.core.ChannelHandler
import ru.capjack.tool.io.InputByteBuffer

object DummyChannelHandler : ChannelHandler {
	override fun handleChannelInput(data: InputByteBuffer) {
		data.skipRead()
	}
	
	override fun handleChannelClose() {}
}
