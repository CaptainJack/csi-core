package ru.capjack.tool.csi.core.client

import ru.capjack.tool.csi.core.client.stubs.PdConnectionProducer
import ru.capjack.tool.csi.core.client.utils.byteArrayOf
import ru.capjack.tool.csi.core.client.utils.clientAcceptor
import ru.capjack.tool.csi.core.client.utils.clientConnector
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFailConnecting {
	
	private fun test(cp: PdConnectionProducer, expected: ConnectFailReason, authorizationKey: ByteArray) {
		var actual: ConnectFailReason? = null
		
		clientConnector(cp).connectClient(authorizationKey, clientAcceptor(onFail = {
			actual = it
		}))
		
		cp.checkAllConnectionsCompleted(300)
		
		assertEquals(expected, actual)
	}
	
	@Test
	fun `Fail of CONNECTION_REFUSED on non connection`() {
		val cp = object : ConnectionProducer {
			override fun produceConnection(acceptor: ConnectionAcceptor) {
				acceptor.acceptFail()
			}
		}
		
		var actual: ConnectFailReason? = null
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor(onFail = {
			actual = it
		}))
		
		assertEquals(ConnectFailReason.CONNECTION_REFUSED, actual)
	}
	
	@Test
	fun `Fail of CONNECTION_REFUSED on close connection`() {
		val cp = PdConnectionProducer().next {
			oClose()
			iData("01 00 00 00 01 99")
			iClose()
		}
		
		test(cp, ConnectFailReason.CONNECTION_REFUSED, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of CONNECTION_REFUSED on CLOSE`() {
		val cp = PdConnectionProducer().next {
			oData("10")
			oClose()
			iData("01 00 00 00 01 99")
			iClose()
		}
		
		test(cp, ConnectFailReason.CONNECTION_REFUSED, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of CONNECTION_REFUSED on SERVER_SHUTDOWN`() {
		val cp = PdConnectionProducer().next {
			oData("11")
			oClose()
			iData("01 00 00 00 01 99")
			iClose()
		}
		
		test(cp, ConnectFailReason.CONNECTION_REFUSED, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of CONNECTION_REFUSED on ACTIVITY_TIMEOUT_EXPIRED`() {
		val cp = PdConnectionProducer().next {
			oData("12")
			oClose()
			iData("01 00 00 00 01 99")
			iClose()
		}
		
		test(cp, ConnectFailReason.CONNECTION_REFUSED, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of CONNECTION_REFUSED on CONCURRENT`() {
		val cp = PdConnectionProducer().next {
			oData("15")
			oClose()
			iData("01 00 00 00 01 99")
			iClose()
		}
		
		test(cp, ConnectFailReason.CONNECTION_REFUSED, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of AUTHORIZATION_REJECTED`() {
		val cp = PdConnectionProducer().next {
			iData("01 00 00 00 01 99")
			oData("13")
			oClose()
			iClose()
		}
		
		test(cp, ConnectFailReason.AUTHORIZATION_REJECTED, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of PROTOCOL_BROKEN`() {
		val cp = PdConnectionProducer().next {
			iData("01 00 00 00 01 99")
			oData("16")
			iClose()
		}
		
		test(cp, ConnectFailReason.PROTOCOL_BROKEN, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of PROTOCOL_BROKEN on invalid server answer`() {
		val cp = PdConnectionProducer().next {
			iData("01 00 00 00 01 99")
			oData("33")
			iClose()
		}
		
		test(cp, ConnectFailReason.PROTOCOL_BROKEN, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of PROTOCOL_BROKEN on unexpected close reason`() {
		val cp = PdConnectionProducer().next {
			iData("01 00 00 00 01 99")
			oData("14")
			oClose()
			iClose()
		}
		
		test(cp, ConnectFailReason.PROTOCOL_BROKEN, byteArrayOf("99"))
	}
	
	@Test
	fun `Fail of SERVER_ERROR`() {
		val cp = PdConnectionProducer().next {
			iData("01 00 00 00 01 99")
			oData("17")
			iClose()
		}
		
		test(cp, ConnectFailReason.SERVER_ERROR, byteArrayOf("99"))
	}
}