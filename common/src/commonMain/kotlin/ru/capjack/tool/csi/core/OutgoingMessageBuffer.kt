package ru.capjack.tool.csi.core

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer

class OutgoingMessageBuffer(
	private var nextMessageId: Int = 1
) : Iterable<OutgoingMessage> {
	
	private val cache = ArrayList<Message>()
	
	private var head: Message? = null
	private var tail: Message? = null
	
	override fun iterator(): Iterator<OutgoingMessage> {
		return object : Iterator<OutgoingMessage> {
			
			private var message = head
			
			override fun hasNext(): Boolean {
				return message != null
			}
			
			override fun next(): OutgoingMessage {
				return message!!.also {
					it.data.rollbackRead()
					message = it.next
				}
			}
		}
	}
	
	fun add(data: Byte): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(1)
			it.data.writeByte(data)
		}
	}
	
	fun add(data: ByteArray): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(data.size)
			it.data.writeArray(data)
		}
	}
	
	fun add(data: InputByteBuffer): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(data.readableSize)
			it.data.writeBuffer(data)
		}
	}
	
	fun clearTo(messageId: Int) {
		var message = head
		while (message != null) {
			if (message.id == messageId) {
				head = message.next
				if (head == null) {
					tail = null
				}
				
				while (message != null) {
					cache.add(message)
					message.data.clear()
					message.next?.prev = null
					message.next = null
					message = message.prev
				}
				
				break
			}
			else {
				message = message.next
			}
		}
	}
	
	fun clear() {
		head = null
		tail = null
	}
	
	private fun provideMessage(): Message {
		val message =
			if (cache.isEmpty()) Message()
			else cache.removeAt(cache.lastIndex)
		
		message.id = nextMessageId++
		if (message.id == 0) {
			message.id = nextMessageId++
		}
		
		if (head == null) {
			head = message
		}
		
		message.prev = tail
		
		tail?.next = message
		tail = message
		
		return message
	}
	
	private class Message : OutgoingMessage {
		var prev: Message? = null
		var next: Message? = null
		
		override val data = ByteBuffer()
		
		override var id: Int = 0
			set(value) {
				field = value
				data.writeByte(ProtocolFlag.MESSAGE)
				data.writeInt(id)
			}
	}
}
