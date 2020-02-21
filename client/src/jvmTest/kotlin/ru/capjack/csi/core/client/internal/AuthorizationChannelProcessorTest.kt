package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.csi.core.client._test.FnInternalChannel
import ru.capjack.csi.core.client._test.GLOBAL_ASSISTANT
import ru.capjack.csi.core.client._test.GLOBAL_BYTE_BUFFER_POOL
import ru.capjack.csi.core.client._test.buffer
import ru.capjack.csi.core.client._test.gate
import ru.capjack.csi.core.client._test.waitIfSecond
import ru.capjack.csi.core.client._test.write
import ru.capjack.csi.core.common.NothingInternalChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthorizationChannelProcessorTest {
	@Test
	fun `Accept connection on valid separated input data`() {
		var connectionId: Long? = null
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptConnection(connection: Connection): ConnectionHandler {
				connectionId = connection.id
				return NothingConnectionHandler()
			}
		}
		
		val channel = FnInternalChannel()
		val processor = AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
		val buffer = buffer {}
		
		buffer.write("10  00 00 00 00 00")
		processor.processChannelInput(channel, buffer)
		
		buffer.write("00 00 07")
		processor.processChannelInput(channel, buffer)
		
		waitIfSecond { connectionId == null }
		
		assertEquals(7, connectionId)
	}
	
	
	@Test
	fun `Connect fail as VERSION on marker SERVER_CLOSE_VERSION`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(close = { actualClosed = true })
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("50"))
		
		assertEquals(ConnectFailReason.VERSION, actualReason)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as AUTHORIZATION on marker SERVER_CLOSE_AUTHORIZATION`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(close = { actualClosed = true })
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("51"))
		
		assertEquals(ConnectFailReason.AUTHORIZATION, actualReason)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as REFUSED on marker SERVER_CLOSE_SHUTDOWN`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(close = { actualClosed = true })
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("54"))
		
		assertEquals(ConnectFailReason.REFUSED, actualReason)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as ERROR on marker CLOSE_ERROR`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(close = { actualClosed = true })
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("32"))
		
		assertEquals(ConnectFailReason.ERROR, actualReason)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as ERROR on marker CLOSE_PROTOCOL_BROKEN`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(close = { actualClosed = true })
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("31"))
		
		assertEquals(ConnectFailReason.ERROR, actualReason)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as REFUSED on marker SERVER_SHUTDOWN_TIMEOUT`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		var actualOutput: Byte? = null
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(
			send = { actualOutput = this.readByte() },
			close = { actualClosed = true }
		)
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("40"))
		
		assertEquals(ConnectFailReason.REFUSED, actualReason)
		assertEquals(0x30, actualOutput)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as ERROR on invalid marker`() {
		var actualReason: ConnectFailReason? = null
		var actualClosed = false
		var actualOutput: Byte? = null
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel(
			send = { actualOutput = this.readByte() },
			close = { actualClosed = true }
		)
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelInput(channel, buffer("77"))
		
		assertEquals(ConnectFailReason.ERROR, actualReason)
		assertEquals(0x31, actualOutput)
		assertTrue(actualClosed)
	}
	
	@Test
	fun `Connect fail as REFUSED on channel interrupted`() {
		var actualReason: ConnectFailReason? = null
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
			.processChannelClose(NothingInternalChannel, true)
		
		assertEquals(ConnectFailReason.REFUSED, actualReason)
	}
	
	@Test
	fun `Connect fail as REFUSED on channel close`() {
		var actualReason: ConnectFailReason? = null
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		val channel = FnInternalChannel()
		
		val processor = AuthorizationChannelProcessor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, acceptor, 1)
		channel.useProcessor(processor)
		processor.processChannelInput(channel, buffer("30"))
		
		assertEquals(ConnectFailReason.REFUSED, actualReason)
	}
}
