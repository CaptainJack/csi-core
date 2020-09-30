package ru.capjack.csi.core.server

import org.junit.Test
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.server._test.FakeDelayableAssistant
import ru.capjack.csi.core.server._test.GLOBAL_BYTE_BUFFER_POOL
import ru.capjack.csi.core.server._test.TestConnectionAuthorizer
import ru.capjack.csi.core.server.internal.Channels
import ru.capjack.csi.core.server.internal.ConnectionAuthorizationAcceptor
import ru.capjack.csi.core.server.internal.ConnectionRecoveryAcceptor
import ru.capjack.csi.core.server.internal.ServerConnection
import ru.capjack.tool.utils.Sluice

class CoverageChannels {
	@Test(expected = IllegalArgumentException::class)
	fun `Coverage bad activityTimeoutSeconds`() {
		Channels(
			Sluice(false),
			GLOBAL_BYTE_BUFFER_POOL,
			FakeDelayableAssistant,
			1,
			0,
			1,
			1,
			TestConnectionAuthorizer(),
			object : ConnectionAuthorizationAcceptor<Int> {
				override fun acceptAuthorization(channel: InternalChannel, identity: Int): ChannelProcessor = throw UnsupportedOperationException()
			},
			object : ConnectionRecoveryAcceptor {
				override fun acceptRecovery(connectionId: Long): ServerConnection<*>? = null
			}
		)
	}
	
	@Test(expected = IllegalArgumentException::class)
	fun `Coverage bad shutdownTimeoutSeconds`() {
		Channels(
			Sluice(false),
			GLOBAL_BYTE_BUFFER_POOL,
			FakeDelayableAssistant,
			1,
			1,
			-1,
			1,
			TestConnectionAuthorizer(),
			object : ConnectionAuthorizationAcceptor<Int> {
				override fun acceptAuthorization(channel: InternalChannel, identity: Int): ChannelProcessor = throw UnsupportedOperationException()
			},
			object : ConnectionRecoveryAcceptor {
				override fun acceptRecovery(connectionId: Long): ServerConnection<*>? = null
			}
		)
	}
	
	@Test(expected = IllegalArgumentException::class)
	fun `Coverage bad stopTimeoutSeconds`() {
		Channels(
			Sluice(false),
			GLOBAL_BYTE_BUFFER_POOL,
			FakeDelayableAssistant,
			1,
			1,
			1,
			0,
			TestConnectionAuthorizer(),
			object : ConnectionAuthorizationAcceptor<Int> {
				override fun acceptAuthorization(channel: InternalChannel, identity: Int): ChannelProcessor = throw UnsupportedOperationException()
			},
			object : ConnectionRecoveryAcceptor {
				override fun acceptRecovery(connectionId: Long): ServerConnection<*>? = null
			}
		)
	}
}