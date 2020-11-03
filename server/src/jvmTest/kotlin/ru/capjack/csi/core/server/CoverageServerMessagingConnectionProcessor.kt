package ru.capjack.csi.core.server

import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingChannel
import ru.capjack.csi.core.common.NothingInternalConnection
import ru.capjack.csi.core.server._test.FakeTemporalAssistant
import ru.capjack.csi.core.server._test.GLOBAL_BYTE_BUFFER_POOL
import ru.capjack.csi.core.server.internal.NothingConnectionHandler
import ru.capjack.csi.core.server.internal.ServerMessagingConnectionProcessor
import ru.capjack.tool.logging.ownLogger
import kotlin.test.Test

class CoverageServerMessagingConnectionProcessor {
	
	@Test(UnsupportedOperationException::class)
	fun `Unsupported processConnectionAccept`() {
		ServerMessagingConnectionProcessor(NothingConnectionHandler, Messages(GLOBAL_BYTE_BUFFER_POOL), ownLogger, FakeTemporalAssistant, 1, 1)
			.processConnectionAccept(
				NothingChannel,
				NothingInternalConnection
			)
	}
}