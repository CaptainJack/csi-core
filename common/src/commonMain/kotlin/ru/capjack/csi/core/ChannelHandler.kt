package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer

interface ChannelHandler {
	fun handleChannelInput(data: InputByteBuffer)
	
	fun handleChannelClose()
}