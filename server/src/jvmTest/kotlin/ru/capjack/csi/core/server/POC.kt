package ru.capjack.csi.core.server

import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.csi.core.server.internal.DummyConnectionDelegate
import ru.capjack.csi.core.server.internal.DummyConnectionHandler
import ru.capjack.csi.core.server.internal.NothingConnectionDelegate
import ru.capjack.csi.core.server.internal.NothingConnectionProcessor
import ru.capjack.csi.core.server.internal.NothingInternalClientProcessor
import ru.capjack.csi.core.server.stubs.DummyConnectionProcessor
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.FramedArrayByteBuffer
import kotlin.test.Test
import kotlin.test.assertFails

class POC {
	
	@Test
	fun `POC FakeConnectionDelegate`() {
		DummyConnectionDelegate.setProcessor(DummyConnectionProcessor())
		DummyConnectionDelegate.send(1)
		DummyConnectionDelegate.send(byteArrayOf(1))
		DummyConnectionDelegate.close()
	}
	
	@Test
	fun `POC FakeConnectionHandler`() {
		DummyConnectionHandler.handleInput(ArrayByteBuffer())
		DummyConnectionHandler.handleClose()
	}
	
	@Test
	fun `POC NothingConnectionDelegate`() {
		assertFails { NothingConnectionDelegate.setProcessor(DummyConnectionProcessor()) }
		assertFails { NothingConnectionDelegate.send(1) }
		assertFails { NothingConnectionDelegate.send(byteArrayOf(1)) }
		assertFails { NothingConnectionDelegate.send(ArrayByteBuffer(0)) }
		assertFails { NothingConnectionDelegate.close(ConnectionCloseReason.CLOSE) }
		assertFails{ NothingConnectionDelegate.close() }
	}
	
	@Test
	fun `POC NothingConnectionProcessor`() {
		assertFails { NothingConnectionProcessor.processInput(DummyConnectionDelegate, FramedArrayByteBuffer(0)) }
		assertFails { NothingConnectionProcessor.processClose(DummyConnectionDelegate, false) }
	}
	
	@Test
	fun `POC NothingInternalClientProcessor`() {
		assertFails { NothingInternalClientProcessor.processInput(DummyConnectionDelegate, FramedArrayByteBuffer(0)) }
		assertFails { NothingInternalClientProcessor.processLoss() }
		assertFails { NothingInternalClientProcessor.processRecovery() }
	}
}