package ru.capjack.tool.csi.common

import ru.capjack.tool.io.InputByteBuffer

interface ConnectionHandler {
	fun handleInput(data: InputByteBuffer)
	
	fun handleClose()
}