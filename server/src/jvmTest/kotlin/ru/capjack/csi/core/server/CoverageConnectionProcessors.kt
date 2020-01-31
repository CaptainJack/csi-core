package ru.capjack.csi.core.server

import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingChannel
import ru.capjack.csi.core.common.NothingConnection
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.csi.core.common.NothingInternalConnection
import ru.capjack.csi.core.server.internal.AcceptationConnectionProcessor
import ru.capjack.csi.core.server._test.FakeDelayableAssistant
import ru.capjack.csi.core.server.internal.RecoveryConnectionProcessor
import ru.capjack.csi.core.server._test.TestConnectionAcceptor
import ru.capjack.tool.io.FramedArrayByteBuffer
import kotlin.test.Test

class CoverageConnectionProcessors {
	
	@Test(expected = UnsupportedOperationException::class)
	fun `AcceptationConnectionProcessor processConnectionRecovery`() {
		AcceptationConnectionProcessor(
			FakeDelayableAssistant,
			TestConnectionAcceptor(),
			1,
			1,
			1
		).processConnectionRecovery(NothingChannel, 0)
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processConnectionAccept`() {
		RecoveryConnectionProcessor(
			NothingInternalConnection,
			NothingConnectionProcessor,
			FakeDelayableAssistant,
			1
		).processConnectionAccept(
			NothingChannel,
			NothingConnection,
			Messages()
		)
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processChannelInput`() {
		RecoveryConnectionProcessor(
			NothingInternalConnection,
			NothingConnectionProcessor,
			FakeDelayableAssistant,
			1
		).processChannelInput(NothingChannel, FramedArrayByteBuffer(0))
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processChannelClose`() {
		RecoveryConnectionProcessor(
			NothingInternalConnection,
			NothingConnectionProcessor,
			FakeDelayableAssistant,
			1
		).processChannelClose(NothingInternalConnection)
	}
}