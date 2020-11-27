package ru.capjack.csi.core.common

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.DummyByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.pool.ObjectPool

class OutgoingMessageBuffer(
	private val byteBuffers: ObjectPool<ByteBuffer>,
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
		return provideMessage(1).also {
			it.data.writeByte(data)
		}
	}
	
	fun add(data: ByteArray): OutgoingMessage {
		return provideMessage(data.size).also {
			it.data.writeArray(data)
		}
	}
	
	fun add(data: InputByteBuffer): OutgoingMessage {
		return provideMessage(data.readableSize).also {
			it.data.writeBuffer(data)
		}
	}
	
	fun clearTo(messageId: Int) {
		var message = head
		
		if (message != null) {
			if (message.id == messageId) {
				head = message.next
				message.clear()
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
						message.clear()
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
	
	fun dispose() {
		var current = head
		while (current != null) {
			val next = current.next
			current.dispose(byteBuffers)
			current = next
		}
		
		cache.forEach { it.dispose(byteBuffers) }
		cache.clear()
		
		head = null
		tail = null
	}
	
	private fun provideMessage(size: Int): Message {
		val message =
			if (cache.isEmpty()) Message(byteBuffers.take())
			else cache.removeAt(cache.lastIndex)
		
		message.prepare(nextMessageId++, size)
		
		if (head == null) {
			head = message
		}
		
		message.prev = tail
		
		tail?.next = message
		tail = message
		
		return message
	}
	
	private class Message(
		override var data: ByteBuffer
	) : OutgoingMessage {
		
		override var id = 0
		override var size = 0
		
		var prev: Message? = null
		var next: Message? = null
		
		fun prepare(id: Int, size: Int) {
			this.id = id
			this.size = size
			
			data.writeByte(ProtocolMarker.MESSAGING_NEW)
			data.writeInt(id)
			data.writeInt(size)
		}
		
		fun reset() {
			data.backRead((size + 1 + 4 + 4) - data.readableSize)
		}
		
		fun clear() {
			id = 0
			size = 0
			next = null
			prev = null
			data.clear()
		}
		
		fun dispose(byteBuffers: ObjectPool<ByteBuffer>) {
			clear()
			byteBuffers.back(data)
			data = DummyByteBuffer
		}
	}
}
