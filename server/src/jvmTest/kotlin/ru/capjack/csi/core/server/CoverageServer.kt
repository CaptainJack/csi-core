package ru.capjack.csi.core.server

import org.junit.Test
import ru.capjack.csi.core.server._test.Errors
import ru.capjack.csi.core.server._test.TestChannelGate
import ru.capjack.csi.core.server._test.TestConnectionAcceptor
import ru.capjack.csi.core.server._test.TestConnectionAuthorizer
import ru.capjack.csi.core.server._test.assistant

class CoverageServer {
	@Test
	fun `Coverage default values in constructor`() {
		Server(
			assistant(2, "server"),
			TestConnectionAuthorizer(),
			TestConnectionAcceptor(),
			TestChannelGate(Errors()),
			1,
			1
		).stop()
	}
}