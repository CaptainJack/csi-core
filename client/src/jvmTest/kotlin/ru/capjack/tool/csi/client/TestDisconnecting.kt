package ru.capjack.tool.csi.client

import ru.capjack.tool.csi.client.stubs.PdConnection
import ru.capjack.tool.csi.client.stubs.PdConnectionProducer
import ru.capjack.tool.csi.client.stubs.StubClientHandler
import ru.capjack.tool.csi.client.utils.clientAcceptor
import ru.capjack.tool.csi.client.utils.clientConnector
import ru.capjack.tool.lang.waitIf
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class TestDisconnecting {
	private fun test(expected: ClientDisconnectReason, steps: PdConnection.Actions.() -> Unit) {
		val cp = PdConnectionProducer().next {
			auth(1000)
			steps()
			iClose()
		}
		
		test(cp, expected)
	}
	
	private fun test(cp: PdConnectionProducer, expected: ClientDisconnectReason) {
		var actual: ClientDisconnectReason? = null
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor({
			object : StubClientHandler(it) {
				override fun handleDisconnect(reason: ClientDisconnectReason) {
					actual = reason
				}
			}
		}))
		
		cp.checkAllConnectionsCompleted(200)
		
		assertEquals(expected, actual)
	}
	
	@Test
	fun `Disconnect from client`() {
		val cp = PdConnectionProducer().next {
			auth(1000)
			iData("10")
			iClose()
		}
		
		var client: Client? = null
		var disconnectReason: ClientDisconnectReason? = null
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor({
			client = it
			object : StubClientHandler(it) {
				override fun handleDisconnect(reason: ClientDisconnectReason) {
					this.client.disconnect()
					disconnectReason = reason
				}
			}
		}))
		
		waitIf(400) { client == null }
		
		client!!.disconnect()
		
		cp.checkAllConnectionsCompleted(400)
		
		assertEquals(ClientDisconnectReason.CLOSE, disconnectReason)
	}
	
	@Test
	fun `Disconnect from client on recovery`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
		
		var disconnectReason: ClientDisconnectReason? = null
		
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor({
			object : StubClientHandler(it) {
				override fun handleDisconnect(reason: ClientDisconnectReason) {
					disconnectReason = reason
				}
				
				override fun handleConnectionLost(): ConnectionRecoveryHandler {
					this.client.disconnect()
					return super.handleConnectionLost()
				}
			}
		}))
		
		cp.checkAllConnectionsCompleted(300)
		
		assertEquals(ClientDisconnectReason.CLOSE, disconnectReason)
	}
	
	@Test
	fun `Disconnect from client on recovery later`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				iData("10")
				iClose()
			}
		
		var disconnectReason: ClientDisconnectReason? = null
		
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor({
			object : StubClientHandler(it) {
				override fun handleDisconnect(reason: ClientDisconnectReason) {
					disconnectReason = reason
				}
				
				override fun handleConnectionLost(): ConnectionRecoveryHandler {
					thread {
						sleep(10)
						this.client.disconnect()
					}
					return super.handleConnectionLost()
				}
			}
		}))
		
		cp.checkAllConnectionsCompleted(100)
		
		assertEquals(ClientDisconnectReason.CLOSE, disconnectReason)
	}
	
	@Test
	fun `Disconnect from client on messaging`() {
		test(ClientDisconnectReason.CLOSE) {
			oData(
				"" +
					"02  00 00 00 01  00 00 00 01  00" +
					"02  00 00 00 02  00 00 00 01  06"
			)
			iData("03  00 00 00 01")
			iData("10")
		}
	}
	
	@Test
	fun `Disconnect of CLOSE`() {
		test(ClientDisconnectReason.CLOSE) {
			oData("10")
		}
	}
	
	@Test
	fun `Disconnect of SERVER_SHUTDOWN`() {
		test(ClientDisconnectReason.SERVER_SHUTDOWN) {
			oData("11")
		}
	}
	
	@Test
	fun `Disconnect of CONNECTION_LOST`() {
		test(ClientDisconnectReason.CONNECTION_LOST) {
			oData("12")
		}
	}
	
	@Test
	fun `Disconnect of PROTOCOL_BROKEN`() {
		test(ClientDisconnectReason.PROTOCOL_BROKEN) {
			oData("16")
		}
	}
	
	@Test
	fun `Disconnect of CONCURRENT`() {
		test(ClientDisconnectReason.CONCURRENT) {
			oData("15")
		}
	}
	
	@Test
	fun `Disconnect of SERVER_ERROR`() {
		test(ClientDisconnectReason.SERVER_ERROR) {
			oData("17")
		}
	}
	
	@Test
	fun `Disconnect of unexpected close reason`() {
		test(ClientDisconnectReason.PROTOCOL_BROKEN) {
			oData("13")
		}
	}
	
	@Test
	fun `Disconnect on recovery of CLOSE`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("10")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.CLOSE)
	}
	
	
	@Test
	fun `Disconnect on recovery of CONNECTION_LOST`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oClose()
				iClose()
			}
		
		test(cp, ClientDisconnectReason.CONNECTION_LOST)
	}
	
	@Test
	fun `Disconnect on recovery of SERVER_ERROR`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("17")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.SERVER_ERROR)
	}
	
	@Test
	fun `Disconnect on recovery of CONCURRENT`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("15")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.CONNECTION_LOST)
	}
	
	@Test
	fun `Disconnect on recovery of SERVER_SHUTDOWN`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("11")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.SERVER_SHUTDOWN)
	}
	
	
	@Test
	fun `Disconnect on recovery of PROTOCOL_BROKEN`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("16")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.PROTOCOL_BROKEN)
	}
	
	
	@Test
	fun `Disconnect on recovery of RECOVERY_REJECT`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("14")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.CONNECTION_LOST)
	}
	
	@Test
	fun `Disconnect on recovery of ACTIVITY_TIMEOUT_EXPIRED`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("12")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.CONNECTION_LOST)
	}
	
	@Test
	fun `Disconnect on recovery of unexpected close reason`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 00")
				oData("13")
				iClose()
			}
		
		test(cp, ClientDisconnectReason.PROTOCOL_BROKEN)
	}
}