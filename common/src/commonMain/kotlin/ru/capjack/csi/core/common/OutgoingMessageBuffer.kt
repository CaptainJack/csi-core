package ru.capjack.csi.core.common

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.DummyByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.concurrency.ObjectPool

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
	
	private fun provideMessage(): Message {
		val message =
			if (cache.isEmpty()) Message(byteBuffers.take())
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
	
	private class Message(data: ByteBuffer) : OutgoingMessage {
		private var _data: ByteBuffer = data
		private var _size: Int = 0
		
		var prev: Message? = null
		var next: Message? = null
		
		override val data get() = _data
		override val size get() = _size
		
		override var id: Int = 0
			set(value) {
				field = value
				_data.writeByte(ProtocolMarker.MESSAGING_NEW)
				_data.writeInt(id)
			}
		
		fun commit() {
			_size = _data.readableSize
		}
		
		fun reset() {
			_data.backRead(_size - _data.readableSize)
		}
		
		fun clear() {
			id = 0
			_size = 0
			next = null
			prev = null
			_data.clear()
		}
		
		fun dispose(byteBuffers: ObjectPool<ByteBuffer>) {
			clear()
			byteBuffers.back(_data)
			_data = DummyByteBuffer
		}
	}
}
