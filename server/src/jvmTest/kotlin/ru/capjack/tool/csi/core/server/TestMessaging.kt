package ru.capjack.tool.csi.core.server

import ru.capjack.tool.csi.core.server.stubs.PdConnectionGateway
import ru.capjack.tool.csi.core.server.utils.createServer
import ru.capjack.tool.io.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMessaging {
	@Test
	fun `With many incoming messages response one last message id`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
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
		}
		
		Thread.sleep(100)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Resend messages after recovery`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		val sid = ByteBuffer()
		
		gateway.produceConnection {
			auth(42) { s, _ -> sid.writeArray(s) }
			
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
		
		Thread.sleep(200)
		
		gateway.checkAllConnectionsCompleted()
		
		assertEquals(0, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.produceConnection {
			oData("07")
			oData(ByteBuffer(20) {
				writeBuffer(sid)
				writeInt(2)
			})
			iData("07  00 00 00 00 00 00 00 2A")
			iData(8 + 4) {}
			
			iData("02  00 00 00 04  00 00 00 01  12")
			iData("02  00 00 00 05  00 00 00 02  34 56")
			iData("02  00 00 00 06  00 00 00 02  78 90")
			
			oData("02  00 00 00 07  00 00 00 02  01  12")
			iData("02  00 00 00 07  00 00 00 01  12")
			iData("03  00 00 00 07")
		}
		
		Thread.sleep(100)
		
		
		assertEquals(1, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Ping stay connection as active`() {
		val gateway = PdConnectionGateway()
		createServer(100, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			
			repeat(3) {
				oData("06")
				iData("06")
				sleep(50)
			}
		}
		
		Thread.sleep(400)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Close on receive close flag`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			
			oData("10")
			iClose()
		}
		
		Thread.sleep(100)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(0, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Receive messages on concurrency`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			
			oData("02  00 00 00 01  00 00 00 05  04  00 00 00 64", true)
			sleep(10)
			oData("02  00 00 00 02  00 00 00 02  01  77")
			
			iData("03  00 00 00 01")
			iData("02  00 00 00 01  00 00 00 01  77")
			iData("03  00 00 00 02")
		}
		
		Thread.sleep(300)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Send messages on concurrency`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			
			oData(
				"" +
					"02  00 00 00 01  00 00 00 02  11  71" +
					"02  00 00 00 02  00 00 00 02  12  72" +
					"02  00 00 00 03  00 00 00 02  13  73"
			)
			
			iData("03  00 00 00 03")
			iData("02  00 00 00 01  00 00 00 01  71")
			iData("02  00 00 00 02  00 00 00 01  72")
			iData("02  00 00 00 03  00 00 00 01  73")
		}
		
		Thread.sleep(200)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Server error on message processing`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			
			oData("02  00 00 00 00  00 00 00 01  05")
			iData("17")
			iClose()
		}
		
		Thread.sleep(100)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Protocol broken on bad flag`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			
			oData("99")
			iData("16")
			iClose()
		}
		
		Thread.sleep(100)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Disconnect client on handle message`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			oData("" +
				"02  00 00 00 01  00 00 00 01  00" +
				"02  00 00 00 02  00 00 00 01  00")
			iData("03  00 00 00 01")
			iData("10")
			iClose()
		}
		
		Thread.sleep(100)
		
		gateway.checkAllConnectionsCompleted()
	}
}
