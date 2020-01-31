package ru.capjack.csi.core.client

import ru.capjack.csi.core.client._test.GLOBAL_ASSISTANT
import ru.capjack.csi.core.client._test.gate
import ru.capjack.csi.core.client._test.waitIfSecond
import ru.capjack.csi.core.client.internal.NothingConnectionAcceptor
import kotlin.test.Test
import kotlin.test.assertTrue

class TestClient {
	@Test
	fun `Call connect open channel`() {
		var called = false
		
		val client = Client(GLOBAL_ASSISTANT, gate {
			called = true
		})
		
		client.connect(byteArrayOf(), NothingConnectionAcceptor())
		
		waitIfSecond { !called }
		
		assertTrue(called)
	}
}