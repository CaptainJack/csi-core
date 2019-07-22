package ru.capjack.tool.csi.client

import ru.capjack.tool.csi.client.internal.DummyConnectionDelegate
import ru.capjack.tool.csi.client.internal.InternalClientImpl
import ru.capjack.tool.csi.client.stubs.DummyConnection
import ru.capjack.tool.csi.client.stubs.DummyConnectionProducer
import ru.capjack.tool.csi.client.stubs.DummyScheduledExecutor
import ru.capjack.tool.csi.client.stubs.PdConnectionProducer
import ru.capjack.tool.csi.client.utils.byteArrayOf
import ru.capjack.tool.csi.client.utils.clientAcceptor
import ru.capjack.tool.csi.client.utils.clientConnector
import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.io.FramedByteBuffer
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.test.Test

class TestInternalClient {
	@Test
	fun `POC checkActivity `() {
		val cp = PdConnectionProducer().next {
			auth(50)
			oData("02  00 00 00 01  00 00 00 05  07  00 00 00 64")
			iData("03 00 00 00 01")
			iData("10")
			iClose()
		}
		
		clientConnector(cp).connectClient(byteArrayOf(), clientAcceptor())
		
		cp.checkAllConnectionsCompleted(300)
	}
	
	@Test
	fun `POC processLoss `() {
		val delegate = DummyConnectionDelegate()
		
		val client = InternalClientImpl(
			DummyScheduledExecutor,
			DummyConnectionProducer,
			delegate,
			byteArrayOf(),
			2
		)
		
		client.accept(clientAcceptor())
		
		thread {
			client.processLoss(DummyConnectionDelegate())
			client.processInput(delegate, FramedByteBuffer().apply {
				writeArray(byteArrayOf("02  00 00 00 01  00 00 00 05  07  00 00 00 64"))
			})
		}
		
		sleep(50)
		
		client.processLoss(delegate)
		
		sleep(100)
		
		client.processLoss(delegate)
	}
	
	@Test
	fun `POC RecoveryProcessor `() {
		val delegate = DummyConnectionDelegate()
		
		var client: InternalClientImpl? = null
		
		val cp = object : ConnectionProducer {
			override fun produceConnection(acceptor: ConnectionAcceptor) {
				client!!.disconnect()
				acceptor.acceptSuccess(DummyConnection())
			}
			
		}
		
		client = InternalClientImpl(
			DummyScheduledExecutor,
			cp,
			delegate,
			byteArrayOf(),
			2
		)
		
		client.accept(clientAcceptor())
		
		client.processLoss(delegate)
	}
	
}

