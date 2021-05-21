package ru.capjack.csi.core.server

import ru.capjack.csi.core.internal.NothingChannel
import ru.capjack.csi.core.internal.NothingConnectionProcessor
import ru.capjack.csi.core.internal.NothingInternalConnection
import ru.capjack.csi.core.server._test.FakeTemporalAssistant
import ru.capjack.csi.core.server._test.TestConnectionAcceptor
import ru.capjack.csi.core.server.internal.AcceptationConnectionProcessor
import ru.capjack.csi.core.server.internal.RecoveryConnectionProcessor
import ru.capjack.tool.io.ArrayByteBuffer
import kotlin.test.Test

class CoverageConnectionProcessors {
	
	@Test(expected = UnsupportedOperationException::class)
	fun `AcceptationConnectionProcessor processConnectionRecovery`() {
		AcceptationConnectionProcessor(
			FakeTemporalAssistant,
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
			FakeTemporalAssistant,
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
			FakeTemporalAssistant,
			1
		).processChannelInput(NothingChannel, ArrayByteBuffer(0))
	}
	
	@Test(expected = UnsupportedOperationException::class)
	fun `RecoveryConnectionProcessor processChannelClose`() {
		RecoveryConnectionProcessor(
			NothingConnectionProcessor,
			NothingInternalConnection,
			FakeTemporalAssistant,
			1
		).processChannelInterrupt(NothingInternalConnection)
	}
}