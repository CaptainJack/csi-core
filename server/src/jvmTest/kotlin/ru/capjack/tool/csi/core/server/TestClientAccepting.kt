package ru.capjack.tool.csi.core.server

import ru.capjack.tool.csi.core.server.stubs.PdConnectionGateway
import ru.capjack.tool.csi.core.server.utils.createServer
import ru.capjack.tool.io.ByteBuffer
import java.lang.Thread.sleep
import kotlin.test.Test
import kotlin.test.assertEquals

class TestClientAccepting {
	@Test
	fun `Protocol broken on invalid first byte from connection`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			oData("77")
			iData("16")
			iClose()
		}
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(0, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Reject authorization on bad auth key`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			oData("01  00 00 00 01  00")
			iData("13")
			iClose()
		}
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(0, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Success authorization on valid auth key`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
		}
		
		sleep(100)
		
		assertEquals(1, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Concurrent authorization`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			iData("15")
			iClose()
		}
		
		sleep(100)
		
		gateway.produceConnection {
			auth(42)
		}
		
		sleep(100)
		
		assertEquals(1, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Reject recovery on non client`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			oData("07  00 00 00 00 00 00 00 01")
			oData("00 00 00 00 00 00 00 00  00 00 00 00")
			iData("14")
			iClose()
		}
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Recovery as on lose connection`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		val sid = ByteBuffer()
		
		gateway.produceConnection {
			auth(42) { s, _ ->
				sid.writeArray(s)
			}
			oClose()
			iClose()
		}
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.produceConnection {
			oData("07")
			oData(ByteBuffer(18) {
				writeBuffer(sid)
				writeInt(0)
			})
			iData("07  00 00 00 00 00 00 00 2A")
			iData(8 + 4) {}
		}
		
		sleep(100)
		
		assertEquals(1, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Recovery as active connection`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		val sid = ByteBuffer()
		
		gateway.produceConnection {
			auth(42) { s, _ ->
				sid.writeArray(s)
			}
			iData("15")
			iClose()
		}
		
		sleep(100)
		
		gateway.produceConnection {
			oData("07")
			oData(ByteBuffer(18) {
				writeBuffer(sid)
				writeInt(0)
			})
			iData("07  00 00 00 00 00 00 00 2A")
			iData(8 + 4) {}
		}
		
		sleep(100)
		
		assertEquals(1, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Recovery as active connection and closed new`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		val sid = ByteBuffer()
		
		gateway.produceConnection {
			auth(42) { s, _ ->
				sid.writeArray(s)
			}
			oData("02  00 00 00 00  00 00 00 05  04  00 00 00 FF")
			iData("03  00 00 00 00")
			iData("15")
			iClose()
		}
		
		sleep(50)
		
		gateway.produceConnection {
			oData("07")
			oData(ByteBuffer(18) {
				writeBuffer(sid)
				writeInt(0)
			})
			oClose()
			iClose()
		}
		
		sleep(400)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(0, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Protocol broken on recovery transition input data`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		val sid = ByteBuffer()
		
		gateway.produceConnection {
			auth(42) { s, _ ->
				sid.writeArray(s)
			}
			oData("02  00 00 00 00  00 00 00 05  04  00 00 00 FF")
			iData("03  00 00 00 00")
			iData("15")
			iClose()
		}
		
		sleep(50)
		
		gateway.produceConnection {
			oData("07")
			oData(ByteBuffer(18) {
				writeBuffer(sid)
				writeInt(0)
			})
			oData("FF")
			iData("16")
			iClose()
		}
		
		sleep(400)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(0, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Reject recovery on bad sid`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			oClose()
			iClose()
		}
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.produceConnection {
			oData("07  00 00 00 00 00 00 00 2A  00 00 00 00 00 00 00 00  00 00 00 00")
			iData("14")
			iClose()
		}
		
		sleep(100)
		
		assertEquals(0, server.statistic.connections)
		assertEquals(1, server.statistic.clients)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	@Test
	fun `Reject recovery on disconnected client`() {
		val gateway = PdConnectionGateway()
		createServer(1000, 0, gateway)
		
		val sid = ByteBuffer()
		
		gateway.produceConnection {
			auth(42) { s, _ ->
				sid.writeArray(s)
			}
			oData("02  00 00 00 00  00 00 00 05  07  00 00 00 C8")
			iData("03  00 00 00 00  10")
			iClose()
		}
		
		sleep(50)
		
		gateway.produceConnection {
			oData("07")
			oData(ByteBuffer(18) {
				writeBuffer(sid)
				writeInt(0)
			})
			iData("14")
			iClose()
		}

		sleep(300)
		
		gateway.checkAllConnectionsCompleted()
	}
	
	
	@Test
	fun `Concurrent client and closed new`() {
		val gateway = PdConnectionGateway()
		val server = createServer(1000, 0, gateway)
		
		gateway.produceConnection {
			auth(42)
			oData("02  00 00 00 00  00 00 00 05  04  00 00 00 FF")
			iData("03  00 00 00 00")
			iData("15")
			iClose()
		}
		
		sleep(50)
		
		gateway.produceConnection {
			oData(ByteBuffer() {
				writeByte(1)
				writeInt(9)
				writeByte(1)
				writeLong(42)
			})
			oClose()
			iClose()
		}
		
		sleep(400)
		
		gateway.checkAllConnectionsCompleted()
		
		assertEquals(0, server.statistic.connections)
		assertEquals(0, server.statistic.clients)
	}
}