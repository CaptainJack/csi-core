package ru.capjack.csi.core

import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer

class OutgoingMessageBuffer(
	private var nextMessageId: Int = 1
) : Iterable<OutgoingMessage> {
	
	private val cache = ArrayList<Message>(1)
	
	private var head: Message? = null
	private var tail: Message? = null
	
	override fun iterator(): Iterator<OutgoingMessage> {
		return object : Iterator<OutgoingMessage> {
			
			private var next = head
			
			override fun hasNext(): Boolean {
				return next != null
			}
			
			override fun next(): OutgoingMessage {
				return next!!.also {
					it.reset()
					next = it.next
				}
			}
		}
	}
	
	fun add(data: Byte): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(1)
			it.data.writeByte(data)
			it.commit()
		}
	}
	
	fun add(data: ByteArray): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(data.size)
			it.data.writeArray(data)
			it.commit()
		}
	}
	
	fun add(data: InputByteBuffer): OutgoingMessage {
		return provideMessage().also {
			it.data.writeInt(data.readableSize)
			it.data.writeBuffer(data)
			it.commit()
		}
	}
	
	fun clearTo(messageId: Int) {
		var message = head
		
		if (message != null) {
			if (message.id == messageId) {
				head = message.next
				message.free()
				cache.add(message)
			}
			else {
				while (message != null && message.id != messageId) {
					message = message.next
				}
				
				if (message != null) {
					head = message.next
					
					while (message != null) {
						val prev = message.prev
						message.free()
						cache.add(message)
						message = prev
					}
				}
			}
			
			head.also {
				if (it == null) {
					tail = null
				}
				else {
					it.prev = null
				}
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
		private var size: Int = 0
		
		override val data = ArrayByteBuffer()
		
		override var id: Int = 0
			set(value) {
				field = value
				data.writeByte(ProtocolFlag.MESSAGE)
				data.writeInt(id)
			}
		
		fun commit() {
			size = data.readableSize
		}
		
		fun reset() {
			data.backRead(size - data.readableSize)
		}
		
		fun free() {
			id = 0
			size = 0
			next = null
			prev = null
			data.clear()
		}
	}
}
