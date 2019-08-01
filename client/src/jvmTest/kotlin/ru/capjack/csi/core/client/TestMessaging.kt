package ru.capjack.csi.core.client

import ru.capjack.csi.core.client.stubs.PdConnectionProducer
import ru.capjack.csi.core.client.stubs.StubClientHandler
import ru.capjack.csi.core.client.utils.clientAcceptor
import ru.capjack.csi.core.client.utils.clientConnector
import ru.capjack.tool.io.ArrayByteBuffer
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertTrue

class TestMessaging {
	@Test
	fun `Ping pong stay connection as active`() {
		val cp = PdConnectionProducer().next {
			auth(50)
			repeat(3) {
				iData("06")
				oData("06")
			}
			oData("10")
			iClose()
		}
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor())
		
		sleep(200)
		
		cp.checkAllConnectionsCompleted(100)
	}
	
	@Test
	fun `When not pong connection terminated`() {
		val cp = PdConnectionProducer()
			.next {
				auth(100)
				iData("06")
				oData("06")
				iData("06")
				iClose()
			}
			.next {
			}
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor())
		
		cp.checkAllConnectionsCompleted(1000)
	}
	
	@Test
	fun `With many incoming messages response one last message id`() {
		val cp = PdConnectionProducer().next {
			auth(1000)
			oData(
				"" +
					"02  00 00 00 01  00 00 00 02  01  12" +
					"02  00 00 00 02  00 00 00 03  02  34 56" +
					"02  00 00 00 03  00 00 00 03  03  78 90"
			)
			
			iData("02  00 00 00 01  00 00 00 01  12")
			iData("02  00 00 00 02  00 00 00 02  34 56")
			iData("02  00 00 00 03  00 00 00 02  78 90")
			iData("03  00 00 00 03")
			
			oData("10")
			iClose()
		}
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor())
		
		cp.checkAllConnectionsCompleted(100)
	}
	
	@Test
	fun `Resend messages after recovery`() {
		val cp = PdConnectionProducer()
			.next {
				auth(1000)
				oData(
					"" +
						"02  00 00 00 01  00 00 00 02  01  12" +
						"02  00 00 00 02  00 00 00 03  02  34 56" +
						"02  00 00 00 03  00 00 00 03  03  78 90"
				)
				
				iData("02  00 00 00 01  00 00 00 01  12")
				iData("02  00 00 00 02  00 00 00 02  34 56")
				iData("02  00 00 00 03  00 00 00 02  78 90")
				iData("03  00 00 00 03")
				
				oData("03  00 00 00 03")
				oData(
					"" +
						"02  00 00 00 04  00 00 00 02  01  12" +
						"02  00 00 00 05  00 00 00 03  02  34 56" +
						"02  00 00 00 06  00 00 00 03  03  78 90"
				)
				
				iData("02  00 00 00 04  00 00 00 01  12")
				iData("02  00 00 00 05  00 00 00 02  34 56")
				iData("02  00 00 00 06  00 00 00 02  78 90")
				iData("03  00 00 00 06")
				
				oClose()
				iClose()
			}
			.next {
				iData("07 1122334455667788 1122334455667788 00 00 00 06")
				oData("07 1122334455667788 1122334455667788 00 00 00 02")
				
				iData("02  00 00 00 04  00 00 00 01  12")
				iData("02  00 00 00 05  00 00 00 02  34 56")
				iData("02  00 00 00 06  00 00 00 02  78 90")
				
				oData("10")
				iClose()
			}
		
		var handleConnectionLost = false
		var handleConnectionRecovered = false
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor({
			object : StubClientHandler(it) {
				override fun handleConnectionLost(): ConnectionRecoveryHandler {
					handleConnectionLost = true
					return object : ConnectionRecoveryHandler {
						override fun handleConnectionRecovered() {
							handleConnectionRecovered = true
						}
					}
				}
			}
		}))
		
		cp.checkAllConnectionsCompleted(1000)
		
		assertTrue(handleConnectionLost)
		assertTrue(handleConnectionRecovered)
	}
	
	@Test
	fun `Send messages concurrency`() {
		val cp = PdConnectionProducer()
			.next {
				auth(10000)
				oData("02  00 00 00 01  00 00 ")
				oData("                       00 05  04  00 00 00 C8")
				iData("03  00 00 00 01")
				iData("02  00 00 00 01  00 00 00 01  01")
				iData("02  00 00 00 02  00 00 00 01  02")
				iData("02  00 00 00 03  00 00 00 01  03")
				iData("10")
				iClose()
			}
		
		var client: Client? = null
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor(onSuccess = {
			client = it
			thread {
				sleep(50)
				it.sendMessage(1)
				it.sendMessage(byteArrayOf(2))
				it.sendMessage(ArrayByteBuffer(byteArrayOf(3)))
				it.disconnect()
				it.disconnect()
				it.sendMessage(1)
				it.sendMessage(byteArrayOf(2))
				it.sendMessage(ArrayByteBuffer(byteArrayOf(3)))
			}
			StubClientHandler(it)
		}))
		
		cp.checkAllConnectionsCompleted(500)
		
		client?.let {
			it.sendMessage(1)
			it.sendMessage(byteArrayOf(2))
			it.sendMessage(ArrayByteBuffer(byteArrayOf(3)))
		}
	}
	
	@Test
	fun `Error on message process`() {
		val cp = PdConnectionProducer().next {
			auth(1000)
			oData("02  00 00 00 01  00 00 00 01  05")
			iData("10")
			iClose()
		}
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor())
		
		cp.checkAllConnectionsCompleted(200)
	}
}