package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer

interface ConnectionHandler {
	fun handleInput(data: InputByteBuffer)
	
	fun handleClose()
}