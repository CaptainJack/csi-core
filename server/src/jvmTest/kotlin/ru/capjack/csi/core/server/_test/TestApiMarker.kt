package ru.capjack.csi.core.server._test

object TestApiMarker {
	const val ECHO: Byte = 0x01
	const val SLEEP: Byte = 0x02
	const val CLOSE: Byte = 0x03
	const val CLOSE_SLEEP: Byte = 0x04
	const val SLEEP_CLOSE: Byte = 0x05
	const val ERROR: Byte = 0x06
	const val CLOSE_ERROR: Byte = 0x07
	const val SEND_BYTE: Byte = 0x08
	const val SEND_ARRAY: Byte = 0x09
	const val SEND_BUFFER: Byte = 0x10
}