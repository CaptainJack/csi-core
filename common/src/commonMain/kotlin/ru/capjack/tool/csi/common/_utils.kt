package ru.capjack.tool.csi.common

import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.toHexString

fun formatLoggerMessageBytes(prefix: String, data: Byte): String {
	return StringBuilder(prefix.length + 6)
		.append(prefix)
		.append("1B: ")
		.also { data.toHexString(it) }
		.toString()
}

fun formatLoggerMessageBytes(prefix: String, data: ByteArray): String {
	val size = data.size
	if (size == 0) {
		return prefix + "0B"
	}
	
	val string = StringBuilder(prefix.length + 12 + size * 3)
	string.append(prefix).append(size).append("B:")
	
	for (byte in data) {
		string.append(' ')
		byte.toHexString(string)
	}
	
	return string.toString()
}

fun formatLoggerMessageBytes(prefix: String, data: InputByteBuffer): String {
	val size = data.readableSize
	val string = formatLoggerMessageBytes(prefix, data.readToArray())
	data.rollbackRead(size)
	return string
}