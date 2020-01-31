package ru.capjack.csi.core.common

import ru.capjack.tool.io.putInt

class LastReceivedMessageId {
	var changed: Boolean = false
		private set
	
	var id: Int = 0
		private set
	
	private val message: ByteArray = ByteArray(5).apply { set(0, ProtocolMarker.MESSAGING_RECEIVED) }
	
	fun update(id: Int) {
		this.id = id
		changed = true
	}
	
	fun makeMessage(): ByteArray {
		changed = false
		message.putInt(1, id)
		return message
	}
}