package ru.capjack.csi.core.server

import ru.capjack.csi.core.server.internal.Channels
import ru.capjack.csi.core.server.internal.Connections
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.Closeable
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.Sluice
import kotlin.random.Random

class Server<I : Any>(
	assistant: DelayableAssistant,
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
) {
	private val logger = ownLogger
	private val gate: Closeable
	
	private val sluice = Sluice()
	private val connections = Connections(
		assistant,
		connectionAcceptor,
		channelActivityTimeoutSeconds,
		connectionStopTimeoutSeconds,
		connectionIdGenerator,
		connectionRegistry
	)
	private val channels = Channels(
		sluice,
		assistant,
		version,
		channelActivityTimeoutSeconds,
		shutdownTimeoutSeconds,
		channelStopTimeoutSeconds,
		connectionAuthorizer,
		connections,
		connections
	)
	
	val running: Boolean
		get() = sluice.opened
	
	val channelsAmount: Int
		get() = channels.size
	
	val connectionsAmount: Int
		get() = connections.size
	
	init {
		logger.info("Starting")
		this.gate = gate.openGate(channels)
		logger.info("Started")
	}
	
	fun stop() {
		if (sluice.close()) {
			logger.info("Stopping")
			
			channels.stop()
			connections.stop()
			gate.close()
			
			logger.info("Stopped")
		}
	}
}
