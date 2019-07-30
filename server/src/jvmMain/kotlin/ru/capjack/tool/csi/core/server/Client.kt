package ru.capjack.tool.csi.core.server

import ru.capjack.tool.io.InputByteBuffer

interface Client {
	val id: Long
	
	fun sendMessage(data: Byte)
	
	fun sendMessage(data: ByteArray)
	
	fun sendMessage(data: InputByteBuffer)
	
	fun disconnect()
	
	fun addDisconnectHandler(handler: ClientDisconnectHandler)
}