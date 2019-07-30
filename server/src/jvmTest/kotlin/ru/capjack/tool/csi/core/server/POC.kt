package ru.capjack.tool.csi.core.server

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.csi.core.server.internal.DummyConnectionDelegate
import ru.capjack.tool.csi.core.server.internal.DummyConnectionHandler
import ru.capjack.tool.csi.core.server.internal.NothingConnectionDelegate
import ru.capjack.tool.csi.core.server.internal.NothingConnectionProcessor
import ru.capjack.tool.csi.core.server.internal.NothingInternalClientProcessor
import ru.capjack.tool.csi.core.server.stubs.DummyConnectionProcessor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedByteBuffer
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
		DummyConnectionHandler.handleInput(ByteBuffer())
		DummyConnectionHandler.handleClose()
	}
	
	@Test
	fun `POC NothingConnectionDelegate`() {
		assertFails { NothingConnectionDelegate.setProcessor(DummyConnectionProcessor()) }
		assertFails { NothingConnectionDelegate.send(1) }
		assertFails { NothingConnectionDelegate.send(byteArrayOf(1)) }
		assertFails { NothingConnectionDelegate.send(ByteBuffer(0)) }
		assertFails { NothingConnectionDelegate.close(ConnectionCloseReason.CLOSE) }
		assertFails{ NothingConnectionDelegate.close() }
	}
	
	@Test
	fun `POC NothingConnectionProcessor`() {
		assertFails { NothingConnectionProcessor.processInput(DummyConnectionDelegate, FramedByteBuffer(0)) }
		assertFails { NothingConnectionProcessor.processClose(DummyConnectionDelegate, false) }
	}
	
	@Test
	fun `POC NothingInternalClientProcessor`() {
		assertFails { NothingInternalClientProcessor.processInput(DummyConnectionDelegate, FramedByteBuffer(0)) }
		assertFails { NothingInternalClientProcessor.processLoss() }
		assertFails { NothingInternalClientProcessor.processRecovery() }
	}
}