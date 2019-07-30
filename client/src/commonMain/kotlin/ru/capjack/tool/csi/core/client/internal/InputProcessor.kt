package ru.capjack.tool.csi.core.client.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal interface InputProcessor {
	fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean
}