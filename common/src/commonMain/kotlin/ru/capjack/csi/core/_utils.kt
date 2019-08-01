package ru.capjack.csi.core

import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.lang.toHexString

fun formatLoggerMessageBytes(prefix: String, data: Byte): String {
	return StringBuilder(prefix.length + 6)
		.append(prefix)
		.append("1B: ")
		.also { data.toHexString(it) }
		.toString()
}

fun formatLoggerMessageBytes(prefix: String, data: ByteArray): String {
	return formatLoggerMessageBytes(prefix, data, 0, data.size)
}

fun formatLoggerMessageBytes(prefix: String, data: InputByteBuffer): String {
	val size = data.readableSize
	val view = data.readableArrayView
	return formatLoggerMessageBytes(prefix, view.array, view.readerIndex, size)
}

private fun formatLoggerMessageBytes(prefix: String, array: ByteArray, offset: Int, size: Int): String {
	return when {
		size == 0 -> prefix + "0B"
		size > 32 -> prefix + size + "B"
		else      -> StringBuilder(prefix.length + 12 + size * 3)
			.append(prefix)
			.append(size)
			.append("B:")
			.apply {
				for (i in offset until offset + size) {
					append(' ')
					array[i].toHexString(this)
				}
			}
			.toString()
	}
}