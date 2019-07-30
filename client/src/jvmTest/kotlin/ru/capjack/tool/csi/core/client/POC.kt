package ru.capjack.tool.csi.core.client

import ru.capjack.tool.csi.core.client.internal.DummyConnectionDelegate
import ru.capjack.tool.csi.core.client.internal.DummyConnectionRecoveryHandler
import ru.capjack.tool.csi.core.client.internal.NothingConnectionProcessor
import ru.capjack.tool.csi.core.client.internal.NothingInternalClientProcessor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedByteBuffer
import kotlin.test.Test
import kotlin.test.assertFails

class POC {
	@Test
	fun `POC DummyConnectionDelegate`() {
		val d = DummyConnectionDelegate()
		d.send(0)
		d.send(ByteBuffer(0))
		d.setProcessor(NothingConnectionProcessor())
		d.terminate()
	}
	
	@Test
	fun `POC NothingConnectionProcessor`() {
		val d = NothingConnectionProcessor()
		
		assertFails { d.processInput(DummyConnectionDelegate(), FramedByteBuffer(0)) }
		assertFails { d.processLoss(DummyConnectionDelegate()) }
	}
	
	@Test
	fun `POC NothingInternalClientProcessor`() {
		val d = NothingInternalClientProcessor()
		
		assertFails { d.processInput(DummyConnectionDelegate(), FramedByteBuffer(0)) }
		assertFails { d.processLoss() }
		assertFails { d.processDisconnect(ClientDisconnectReason.CLOSE) }
	}
	
	@Test
	fun `POC DummyConnectionRecoveryHandler`() {
		DummyConnectionRecoveryHandler().handleConnectionRecovered()
	}
}