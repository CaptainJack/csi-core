package ru.capjack.csi.core.server

import org.junit.Test
import ru.capjack.csi.core.server._test.NothingServerChannel
import ru.capjack.csi.core.server.internal.NothingConnectionHandler
import ru.capjack.csi.core.server.internal.NothingServerChannelReleaser
import ru.capjack.tool.io.ArrayByteBuffer

class CoverageNothing {
	@Test(expected = UnsupportedOperationException::class)
	fun `NothingConnectionHandler handleConnectionMessage`() {
		NothingConnectionHandler.handleConnectionMessage(ArrayByteBuffer(0))
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `NothingConnectionHandler handleConnectionClose`() {
		NothingConnectionHandler.handleConnectionClose()
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `NothingServerChannelReleaser handleConnectionClose`() {
		NothingServerChannelReleaser.releaseServerChannel(NothingServerChannel)
	}
}