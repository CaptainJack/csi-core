package ru.capjack.csi.core.server

import org.junit.Test
import ru.capjack.csi.core.common.NothingInternalChannel
import ru.capjack.csi.core.server._test.EmptyConnectionProcessor
import ru.capjack.csi.core.server._test.EmptyInternalChannel
import ru.capjack.csi.core.server._test.EmptyServerConnectionReleaser
import ru.capjack.csi.core.server._test.GLOBAL_BYTE_BUFFER_POOL
import ru.capjack.csi.core.server._test.assistant
import ru.capjack.csi.core.server.internal.ServerConnectionImpl
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.lang.waitIf

class CoverageServerConnectionImpl {
	@Test
	fun `Recovery on closed connection`() {
		val connection = ServerConnectionImpl(
			1,
			1,
			EmptyInternalChannel,
			EmptyConnectionProcessor,
			assistant(),
			GLOBAL_BYTE_BUFFER_POOL,
			EmptyServerConnectionReleaser
		)
		
		var b = true
		connection.close { b = false }
		
		waitIf(1000) { b }
		
		connection.recovery(EmptyInternalChannel, 0)
	}
	
	@Test
	fun `Close on closed connection`() {
		val connection = ServerConnectionImpl(
			1,
			1,
			EmptyInternalChannel,
			EmptyConnectionProcessor,
			assistant(),
			GLOBAL_BYTE_BUFFER_POOL,
			EmptyServerConnectionReleaser
		)
		
		connection.close {
			connection.close()
		}
	}
	
	@Test
	fun `Process close on outside close`() {
		val connection = ServerConnectionImpl(
			1,
			1,
			EmptyInternalChannel,
			EmptyConnectionProcessor,
			assistant(),
			GLOBAL_BYTE_BUFFER_POOL,
			EmptyServerConnectionReleaser
		)
		
		connection.close()
		connection.processChannelClose(EmptyInternalChannel, true)
	}
	
	@Test
	fun `Process input with wrong channel`() {
		val connection = ServerConnectionImpl(
			1,
			1,
			EmptyInternalChannel,
			EmptyConnectionProcessor,
			assistant(),
			GLOBAL_BYTE_BUFFER_POOL,
			EmptyServerConnectionReleaser
		)
		
		connection.processChannelInput(NothingInternalChannel, ArrayByteBuffer(0))
	}
}

