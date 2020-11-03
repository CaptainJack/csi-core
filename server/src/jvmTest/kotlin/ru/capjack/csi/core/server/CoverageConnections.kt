package ru.capjack.csi.core.server

import org.junit.Test
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.NothingInternalChannel
import ru.capjack.csi.core.server._test.FakeTemporalAssistant
import ru.capjack.csi.core.server._test.GLOBAL_BYTE_BUFFER_POOL
import ru.capjack.csi.core.server._test.NowTemporalAssistant
import ru.capjack.csi.core.server._test.TestConnectionAcceptor
import ru.capjack.csi.core.server.internal.Connections
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.assistant.ExecutorTemporalAssistant
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.test.assertNull

class CoverageConnections {
	@Test(expected = IllegalArgumentException::class)
	fun `Coverage bad activityTimeoutSeconds`() {
		Connections(
			FakeTemporalAssistant,
			GLOBAL_BYTE_BUFFER_POOL,
			TestConnectionAcceptor(),
			0,
			1,
			Random.Default::nextLong,
			DummyConnectionRegistry()
		)
	}
	
	@Test(expected = IllegalArgumentException::class)
	fun `Coverage bad stopTimeoutSeconds`() {
		Connections(
			FakeTemporalAssistant,
			GLOBAL_BYTE_BUFFER_POOL,
			TestConnectionAcceptor(),
			1,
			0,
			Random.Default::nextLong,
			DummyConnectionRegistry()
		)
	}
	
	@Test
	fun `Coverage connectionIdGenerator gives invalid ids`() {
		val ids = mutableListOf<Long>(0, 1, 1, 2)
		val connections = Connections(
			FakeTemporalAssistant,
			GLOBAL_BYTE_BUFFER_POOL,
			TestConnectionAcceptor(),
			1,
			1,
			{
				ids.removeAt(0)
			},
			DummyConnectionRegistry()
		)
		
		connections.acceptAuthorization(NothingInternalChannel, 1)
		connections.acceptAuthorization(NothingInternalChannel, 1)
	}
	
	@Test
	fun `Coverage recovery connection on miss identity`() {
		val connections = Connections(
			NowTemporalAssistant,
			GLOBAL_BYTE_BUFFER_POOL,
			TestConnectionAcceptor(),
			1,
			1,
			{ 1 },
			DummyConnectionRegistry()
		)
		
		connections.acceptAuthorization(object : InternalChannel {
			override val id: Any = 1
			
			override fun useProcessor(processor: ChannelProcessor) {}
			
			override fun useProcessor(processor: ChannelProcessor, activityTimeoutSeconds: Int) {}
			
			override fun closeWithMarker(marker: Byte) {}
			
			override fun send(data: Byte) {}
			
			override fun send(data: ByteArray) {
				connections.acceptRecovery(1)
			}
			
			override fun send(data: InputByteBuffer) {
				data.skipRead()
			}
			
			override fun close() {}
		}, 1)
	}
	
	@Test
	fun `Coverage recovery connection on miss connection id`() {
		var i = 0L
		val connections = Connections(
			ExecutorTemporalAssistant(Executors.newSingleThreadScheduledExecutor()),
			GLOBAL_BYTE_BUFFER_POOL,
			TestConnectionAcceptor(),
			1,
			1,
			{ ++i },
			DummyConnectionRegistry()
		)
		
		connections.acceptAuthorization(object : InternalChannel {
			override val id: Any = 1
			
			override fun useProcessor(processor: ChannelProcessor) {}
			
			override fun useProcessor(processor: ChannelProcessor, activityTimeoutSeconds: Int) {}
			
			override fun closeWithMarker(marker: Byte) {
				connections.acceptRecovery(2)
			}
			
			override fun send(data: Byte) {}
			
			override fun send(data: ByteArray) {
				
				connections.acceptAuthorization(object : InternalChannel {
					override val id: Any = 1
					
					override fun useProcessor(processor: ChannelProcessor) {}
					
					override fun useProcessor(processor: ChannelProcessor, activityTimeoutSeconds: Int) {}
					
					override fun closeWithMarker(marker: Byte) {}
					
					override fun send(data: Byte) {}
					
					override fun send(data: ByteArray) {}
					
					override fun send(data: InputByteBuffer) {
						data.skipRead()
					}
					
					override fun close() {}
				}, 1)
				
			}
			
			override fun send(data: InputByteBuffer) {
				data.skipRead()
			}
			
			override fun close() {}
		}, 1)
	}
	
	@Test
	fun `Coverage recovery connection on bad connection id`() {
		var i = 0L
		val connections = Connections(
			ExecutorTemporalAssistant(Executors.newSingleThreadScheduledExecutor()),
			GLOBAL_BYTE_BUFFER_POOL,
			TestConnectionAcceptor(),
			1,
			1,
			{ ++i },
			DummyConnectionRegistry()
		)
		
		connections.acceptAuthorization(object : InternalChannel {
			override val id: Any = 1
			
			override fun useProcessor(processor: ChannelProcessor) {}
			
			override fun useProcessor(processor: ChannelProcessor, activityTimeoutSeconds: Int) {}
			
			override fun closeWithMarker(marker: Byte) {}
			
			override fun send(data: Byte) {}
			
			override fun send(data: ByteArray) {}
			
			override fun send(data: InputByteBuffer) {
				data.skipRead()
			}
			
			override fun close() {}
		}, 1)
		
		
		val identities = Connections::class.java.getDeclaredField("identities")
		identities.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		(identities.get(connections) as MutableMap<Long, Int>)[2] = 1
		
		assertNull(connections.acceptRecovery(2))
	}
}