package ru.capjack.csi.core.client

import ru.capjack.csi.core.client.internal.DummyConnectionDelegate
import ru.capjack.csi.core.client.internal.DummyConnectionRecoveryHandler
import ru.capjack.csi.core.client.internal.NothingConnectionProcessor
import ru.capjack.csi.core.client.internal.NothingInternalClientProcessor
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.FramedArrayByteBuffer
import kotlin.test.Test
import kotlin.test.assertFails

class POC {
	@Test
	fun `POC DummyConnectionDelegate`() {
		val d = DummyConnectionDelegate()
		d.send(0)
		d.send(ArrayByteBuffer(0))
		d.setProcessor(NothingConnectionProcessor())
		d.terminate()
	}
	
	@Test
	fun `POC NothingConnectionProcessor`() {
		val d = NothingConnectionProcessor()
		
		assertFails { d.processInput(DummyConnectionDelegate(), FramedArrayByteBuffer(0)) }
		assertFails { d.processLoss(DummyConnectionDelegate()) }
	}
	
	@Test
	fun `POC NothingInternalClientProcessor`() {
		val d = NothingInternalClientProcessor()
		
		assertFails { d.processInput(DummyConnectionDelegate(), FramedArrayByteBuffer(0)) }
		assertFails { d.processLoss() }
		assertFails { d.processDisconnect(ClientDisconnectReason.CLOSE) }
	}
	
	@Test
	fun `POC DummyConnectionRecoveryHandler`() {
		DummyConnectionRecoveryHandler().handleConnectionRecovered()
	}
}