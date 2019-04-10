package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.io.FramedInputByteBuffer

internal interface InternalClientProcessor {
	fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean
	
	fun processLoss()
	
	fun processRecovery()
}