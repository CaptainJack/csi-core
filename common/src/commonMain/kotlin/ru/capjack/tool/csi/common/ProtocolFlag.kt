package ru.capjack.tool.csi.common

object ProtocolFlag {
	const val AUTHORIZATION: Byte = 0x01
	const val MESSAGE: Byte = 0x02
	const val MESSAGE_RECEIVED: Byte = 0x03
	const val PING: Byte = 0x06
	const val RECOVERY: Byte = 0x07
	const val SERVER_SHUTDOWN_TIMEOUT: Byte = 0x08
	const val CLOSE: Byte = 0x10
	const val CLOSE_SERVER_SHUTDOWN: Byte = 0x11
	const val CLOSE_ACTIVITY_TIMEOUT_EXPIRED: Byte = 0x12
	const val CLOSE_AUTHORIZATION_REJECT: Byte = 0x13
	const val CLOSE_RECOVERY_REJECT: Byte = 0x14
	const val CLOSE_CONCURRENT: Byte = 0x15
	const val CLOSE_PROTOCOL_BROKEN: Byte = 0x16
	const val CLOSE_SERVER_ERROR: Byte = 0x17
}