package ru.capjack.csi.core.client

import org.junit.Test

class TestDummyConnectionRecoveryHandler {
	@Test
	fun `Coverage handleConnectionRecovered`() {
		DummyConnectionRecoveryHandler().handleConnectionRecovered()
	}
}