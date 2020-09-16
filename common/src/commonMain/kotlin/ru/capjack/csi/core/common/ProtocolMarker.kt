package ru.capjack.csi.core.common

import ru.capjack.tool.lang.toHexString

object ProtocolMarker {
	const val AUTHORIZATION: Byte = 0x10
	const val RECOVERY: Byte = 0x11
	
	const val MESSAGING_PING: Byte = 0x20
	const val MESSAGING_NEW: Byte = 0x21
	const val MESSAGING_RECEIVED: Byte = 0x22
	
	const val CLOSE_DEFINITELY: Byte = 0x30
	const val CLOSE_PROTOCOL_BROKEN: Byte = 0x31
	const val CLOSE_ERROR: Byte = 0x32
	const val CLOSE_ACTIVITY_TIMEOUT: Byte = 0x33
	
	const val SERVER_SHUTDOWN_TIMEOUT: Byte = 0x40
	
	const val SERVER_CLOSE_VERSION: Byte = 0x50
	const val SERVER_CLOSE_AUTHORIZATION: Byte = 0x51
	const val SERVER_CLOSE_RECOVERY_FAIL: Byte = 0x52
	const val SERVER_CLOSE_CONCURRENT: Byte = 0x53
	const val SERVER_CLOSE_SHUTDOWN: Byte = 0x54
	
	fun toString(marker: Byte) = "${getName(marker)}(0x${marker.toHexString()})"
	
	fun getName(marker: Byte) = when (marker) {
		AUTHORIZATION              -> "AUTHORIZATION"
		RECOVERY                   -> "RECOVERY"
		MESSAGING_PING             -> "MESSAGING_PING"
		MESSAGING_NEW              -> "MESSAGING_NEW"
		MESSAGING_RECEIVED         -> "MESSAGING_RECEIVED"
		CLOSE_DEFINITELY           -> "CLOSE_DEFINITELY"
		CLOSE_PROTOCOL_BROKEN      -> "CLOSE_PROTOCOL_BROKEN"
		CLOSE_ERROR                -> "CLOSE_ERROR"
		CLOSE_ACTIVITY_TIMEOUT     -> "CLOSE_ACTIVITY_TIMEOUT"
		SERVER_SHUTDOWN_TIMEOUT    -> "SERVER_SHUTDOWN_TIMEOUT"
		SERVER_CLOSE_VERSION       -> "SERVER_CLOSE_VERSION"
		SERVER_CLOSE_AUTHORIZATION -> "SERVER_CLOSE_AUTHORIZATION"
		SERVER_CLOSE_RECOVERY_FAIL -> "SERVER_CLOSE_RECOVERY_FAIL"
		SERVER_CLOSE_CONCURRENT    -> "SERVER_CLOSE_CONCURRENT"
		SERVER_CLOSE_SHUTDOWN      -> "SERVER_CLOSE_SHUTDOWN"
		else                       -> "UNKNOWN"
	}
}
