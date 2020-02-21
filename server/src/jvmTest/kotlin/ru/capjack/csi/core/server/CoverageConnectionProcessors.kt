package ru.capjack.csi.core.server

import ru.capjack.csi.core.common.NothingChannel
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.csi.core.common.NothingInternalConnection
import ru.capjack.csi.core.server._test.FakeDelayableAssistant
import ru.capjack.csi.core.server._test.TestConnectionAcceptor
import ru.capjack.csi.core.server.internal.AcceptationConnectionProcessor
import ru.capjack.csi.core.server.internal.RecoveryConnectionProcessor
import ru.capjack.tool.io.ArrayByteBuffer
import kotlin.test.Test

class CoverageConnectionProcessors {
	
	@Test(expected = UnsupportedOperationException::class)
	fun `AcceptationConnectionProcessor processConnectionRecovery`() {
		AcceptationConnectionProcessor(
			FakeDelayableAssistant,
			TestConnectionAcceptor(),
			1,
			1
		).processConnectionRecovery(NothingChannel)
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processConnectionAccept`() {
		RecoveryConnectionProcessor(
			NothingConnectionProcessor,
			NothingInternalConnection,
			FakeDelayableAssistant,
			1
		).processConnectionAccept(
			NothingChannel,
			NothingInternalConnection
		)
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processChannelInput`() {
		RecoveryConnectionProcessor(
			NothingConnectionProcessor,
			NothingInternalConnection,
			FakeDelayableAssistant,
			1
		).processChannelInput(NothingChannel, ArrayByteBuffer(0))
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processChannelClose`() {
		RecoveryConnectionProcessor(
			NothingConnectionProcessor,
			NothingInternalConnection,
			FakeDelayableAssistant,
			1
		).processChannelInterrupt(NothingInternalConnection)
	}
}