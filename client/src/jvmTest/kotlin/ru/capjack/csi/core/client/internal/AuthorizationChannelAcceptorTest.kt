package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client._test.FnChannel
import ru.capjack.csi.core.client._test.GLOBAL_ASSISTANT
import ru.capjack.csi.core.client._test.GLOBAL_BYTE_BUFFER_POOL
import ru.capjack.csi.core.client._test.assertEqualsBytes
import ru.capjack.csi.core.client._test.gate
import ru.capjack.csi.core.client._test.waitIfSecond
import ru.capjack.tool.io.readToArray
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorizationChannelAcceptorTest {
	@Test
	fun `When accept fail then connect failed as REFUSED`() {
		var actualReason: ConnectFailReason? = null
		
		val acceptor = object : NothingConnectionAcceptor() {
			override fun acceptFail(reason: ConnectFailReason) {
				actualReason = reason
			}
		}
		
		AuthorizationChannelAcceptor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, 0, byteArrayOf(), acceptor, 1)
			.acceptFail()
		
		assertEquals(ConnectFailReason.REFUSED, actualReason)
	}
	
	@Test
	fun `When accept success then send auth request`() {
		var data: ByteArray? = null
		
		val acceptor = NothingConnectionAcceptor()
		
		AuthorizationChannelAcceptor(GLOBAL_ASSISTANT, GLOBAL_BYTE_BUFFER_POOL, gate {}, 7, byteArrayOf(0x42), acceptor, 1)
			.acceptChannel(FnChannel({
				data = readToArray()
			}))
		
		waitIfSecond { data == null }
		
		assertEqualsBytes("10  00 00 00 07  00 00 00 01  42", data)
	}
}