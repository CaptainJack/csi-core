package ru.capjack.csi.core.server

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.MessagingConnectionProcessor
import ru.capjack.csi.core.common.NothingChannel
import ru.capjack.csi.core.common.NothingConnection
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.csi.core.server._test.FakeDelayableAssistant
import ru.capjack.csi.core.server.internal.NothingConnectionHandler
import ru.capjack.csi.core.server.internal.ServerMessagingConnectionProcessor
import kotlin.test.Test

class CoverageServerMessagingConnectionProcessor {
	
	@Test(UnsupportedOperationException::class)
	fun `Unsupported processConnectionAccept`() {
		ServerMessagingConnectionProcessor(NothingConnectionHandler, Messages(), FakeDelayableAssistant, 1)
			.processConnectionAccept(
				NothingChannel,
				NothingConnection,
				Messages()
			)
	}
	
	@Test
	fun `Coverage getHandler`() {
		object : MessagingConnectionProcessor<ConnectionHandler>(NothingConnectionHandler,
			Messages()
		) {
			init {
				this.handler
			}
			
			override fun doProcessConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor =
				NothingConnectionProcessor
			override fun doProcessConnectionClose(): ConnectionHandler = NothingConnectionHandler
			override fun processChannelClose(connection: InternalConnection): ConnectionProcessor =
				NothingConnectionProcessor
		}
	}
}