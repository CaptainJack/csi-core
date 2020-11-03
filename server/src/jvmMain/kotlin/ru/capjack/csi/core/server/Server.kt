package ru.capjack.csi.core.server

import ru.capjack.csi.core.server.internal.Channels
import ru.capjack.csi.core.server.internal.Connections
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.Closeable
import ru.capjack.tool.utils.Sluice
import ru.capjack.tool.utils.Stoppable
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool
import kotlin.random.Random

class Server<I : Any>(
	assistant: TemporalAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	connectionAuthorizer: ConnectionAuthorizer<I>,
	connectionAcceptor: ConnectionAcceptor<I>,
	gate: ChannelGate,
	shutdownTimeoutSeconds: Int = 0,
	version: Int = 0,
	channelActivityTimeoutSeconds: Int = 30,
	channelStopTimeoutSeconds: Int = 60 * 10,
	connectionStopTimeoutSeconds: Int = 60 * 10,
	connectionIdGenerator: () -> Long = Random.Default::nextLong,
	connectionRegistry: ConnectionRegistry<I> = DummyConnectionRegistry()
) : Stoppable {
	private val logger = ownLogger
	private val gate: Closeable
	
	private val sluice = Sluice()
	private val connectionHolder = Connections(
		assistant,
		byteBuffers,
		connectionAcceptor,
		channelActivityTimeoutSeconds,
		connectionStopTimeoutSeconds,
		connectionIdGenerator,
		connectionRegistry
	)
	private val channelHolder = Channels(
		sluice,
		byteBuffers,
		assistant,
		version,
		channelActivityTimeoutSeconds,
		shutdownTimeoutSeconds,
		channelStopTimeoutSeconds,
		connectionAuthorizer,
		connectionHolder,
		connectionHolder
	)
	
	val running: Boolean
		get() = sluice.opened
	
	val channels: Int
		get() = channelHolder.size
	
	val connections: Int
		get() = connectionHolder.size
	
	init {
		logger.debug("Starting")
		this.gate = gate.openGate(channelHolder)
		logger.debug("Started")
	}
	
	override fun stop() {
		if (sluice.close()) {
			logger.debug("Stopping")
			
			channelHolder.stop()
			connectionHolder.stop()
			gate.close()
			
			logger.debug("Stopped")
		}
	}
}
