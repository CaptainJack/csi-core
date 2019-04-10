package ru.capjack.tool.csi.server

import ru.capjack.tool.io.InputByteBuffer

interface ConnectionHandler {
	fun handleInput(data: InputByteBuffer)
	
	fun handleClose()
}