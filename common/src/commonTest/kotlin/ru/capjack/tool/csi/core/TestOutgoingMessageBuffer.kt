package ru.capjack.tool.csi.core

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.readToArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestOutgoingMessageBuffer {
	
	@Test
	fun message_ids_sequential() {
		val buffer = OutgoingMessageBuffer()
		
		val id1 = buffer.add(0).id
		val id2 = buffer.add(0).id
		val id3 = buffer.add(0).id
		
		assertEquals(id1, id2 - 1)
		assertEquals(id2, id3 - 1)
	}
	
	@Test
	fun message_ids_overflow_sign() {
		val buffer = OutgoingMessageBuffer(Int.MAX_VALUE)
		
		val id1 = buffer.add(0).id
		val id2 = buffer.add(0).id
		
		assertEquals(Int.MAX_VALUE, id1)
		assertEquals(Int.MIN_VALUE, id2)
	}
	
	@Test
	fun message_ids_overflow_zero() {
		val buffer = OutgoingMessageBuffer(-1)
		
		val id1 = buffer.add(0).id
		val id2 = buffer.add(0).id
		
		assertEquals(-1, id1)
		assertEquals(1, id2)
	}
	
	@Test
	fun messages_buffered() {
		val buffer = OutgoingMessageBuffer()
		
		val messages = listOf(
			buffer.add(0),
			buffer.add(1),
			buffer.add(2)
		)
		
		assertEquals(messages, buffer.toList())
	}
	
	@Test
	fun clear_all() {
		val buffer = OutgoingMessageBuffer()
		buffer.add(0)
		buffer.add(1)
		buffer.add(2)
		
		buffer.clear()
		
		assertTrue(buffer.toList().isEmpty())
	}
	
	@Test
	fun clear_to() {
		val buffer = OutgoingMessageBuffer()
		
		buffer.add(0)
		buffer.add(1)
		buffer.add(2)
		val id = buffer.add(3).id
		
		val messages = listOf(
			buffer.add(3),
			buffer.add(2),
			buffer.add(1),
			buffer.add(0)
		)
		
		buffer.clearTo(id)
		
		assertEquals(messages, buffer.toList())
	}
	
	@Test
	fun messages_polled() {
		val buffer = OutgoingMessageBuffer()
		
		val m1 = buffer.add(0)
		val m2 = buffer.add(1)
		val m3 = buffer.add(2)
		
		assertNotSame(m1, m2)
		assertNotSame(m2, m3)
		
		buffer.clearTo(m2.id)
		
		val m4 = buffer.add(3)
		val m5 = buffer.add(4)
		val m6 = buffer.add(5)
		
		assertSame(m1, m4)
		assertSame(m2, m5)
		assertNotSame(m3, m6)
	}
	
	@Test
	fun message_format_byte() {
		val buffer = OutgoingMessageBuffer(42)
		
		val data: Byte = 0x0F
		val bytes = buffer.add(data).data.readToArray().toList()
		
		val expected = listOf(
			ProtocolFlag.MESSAGE,
			0x00, 0x00, 0x00, 0x2A,
			0x00, 0x00, 0x00, 0x01,
			0x0F
		)
		
		assertEquals(expected, bytes)
	}
	
	@Test
	fun message_format_bytes() {
		val buffer = OutgoingMessageBuffer(42)
		
		val data = byteArrayOf(0x0F, 0x77)
		val bytes = buffer.add(data).data.readToArray().toList()
		
		val expected = listOf(
			ProtocolFlag.MESSAGE,
			0x00, 0x00, 0x00, 0x2A,
			0x00, 0x00, 0x00, 0x02,
			0x0F, 0x77
		)
		
		assertEquals(expected, bytes)
	}
	
	@Test
	fun message_format_buffer() {
		val buffer = OutgoingMessageBuffer(42)
		
		val data = ByteBuffer {
			writeInt(10266)
		}
		val bytes = buffer.add(data).data.readToArray().toList()
		
		val expected = listOf(
			ProtocolFlag.MESSAGE,
			0x00, 0x00, 0x00, 0x2A,
			0x00, 0x00, 0x00, 0x04,
			0x00, 0x00, 0x28, 0x1A
		)
		
		assertEquals(expected, bytes)
	}
	
	@Test
	fun message_format_after_polled() {
		val buffer = OutgoingMessageBuffer(42)
		
		val m1 = buffer.add(byteArrayOf(0x03, 0x13, 0x50))
		
		buffer.clearTo(m1.id)
		
		val m2 = buffer.add(0x06)
		
		val bytes = m2.data.readToArray().toList()
		val expected = listOf(
			ProtocolFlag.MESSAGE,
			0x00, 0x00, 0x00, 0x2B,
			0x00, 0x00, 0x00, 0x01,
			0x06
		)
		
		assertSame(m1, m2)
		assertEquals(expected, bytes)
	}
	
	@Test
	fun message_readable_on_iterate() {
		val buffer = OutgoingMessageBuffer(42)
		
		val m1 =  buffer.add(0)
		
		assertTrue(m1.data.readable)
		val bytes = m1.data.readToArray().toList()
		assertFalse(m1.data.readable)
		
		buffer.forEach {
			assertTrue(it.data.readable)
			assertEquals(bytes, it.data.readToArray().toList())
			assertFalse(it.data.readable)
		}
		
		buffer.forEach {
			assertTrue(it.data.readable)
			assertEquals(bytes, it.data.readToArray().toList())
			assertFalse(it.data.readable)
		}
	}
}