package ru.capjack.tool.csi.common

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer

class OutgoingMessageBuffer(
	private var nextMessageId: Int = 0
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
					it.data.reset()
					message = it.next
				}
			}
		}
	}
	
	fun add(data: Byte): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(1)
			it.data.writeByte(data)
			it.data.commit()
		}
	}
	
	fun add(data: ByteArray): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(data.size)
			it.data.writeArray(data)
			it.data.commit()
		}
	}
	
	fun add(data: InputByteBuffer): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(data.readableSize)
			it.data.writeBuffer(data)
			it.data.commit()
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
		
		override val data = MessageData()
		
		override var id: Int = 0
			set(value) {
				field = value
				data.writeByte(ProtocolFlag.MESSAGE)
				data.writeInt(id)
			}
	}
	
	private class MessageData : ByteBuffer() {
		private var size: Int = 0
		
		fun commit() {
			size = readableSize
		}
		
		fun reset() {
			rollbackRead(size)
		}
	}
}
