package ru.capjack.csi.core.client

import org.junit.Test

class DummyConnectionRecoveryHandlerTest {
	@Test
	fun `Coverage handleConnectionRecovered`() {
		DummyConnectionRecoveryHandler().handleConnectionRecovered()
	}
}