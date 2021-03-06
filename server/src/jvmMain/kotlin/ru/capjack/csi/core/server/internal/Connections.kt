package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.csi.core.server.ConnectionRegistry
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.lang.waitIfLater
import ru.capjack.tool.logging.info
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.warn
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal class Connections<I : Any>(
	private val assistant: TemporalAssistant,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val acceptor: ConnectionAcceptor<I>,
	private val activityTimeoutSeconds: Int,
	private val stopTimeoutSeconds: Int,
	private val idGenerator: () -> Long,
	private val registry: ConnectionRegistry<I>
) : ConnectionAuthorizationAcceptor<I>, ConnectionRecoveryAcceptor, ServerConnectionReleaser<I> {
	
	private val logger = ownLogger
	private val _size = AtomicInteger()
	private val identities = ConcurrentHashMap<Long, I>()
	private val connections = ConcurrentHashMap<I, ServerConnection<I>>()
	
	val size: Int
		get() = _size.get()
	
	init {
		require(activityTimeoutSeconds > 0)
		require(stopTimeoutSeconds > 0)
	}
	
	override fun acceptAuthorization(channel: InternalChannel, identity: I): InternalChannelProcessor {
		logger.trace { "Accept connection $identity" }
		
		var connectionId: Long
		do {
			connectionId = idGenerator.invoke()
		}
		while (connectionId == 0L || identities.putIfAbsent(connectionId, identity) != null)
		
		val connection = ServerConnectionImpl(
			connectionId,
			identity,
			channel,
			AcceptationConnectionProcessor(assistant, acceptor, activityTimeoutSeconds, identity),
			assistant,
			byteBuffers,
			this
		)
		
		_size.getAndIncrement()
		
		registry.put(identity) {
			connections.compute(identity, AcceptationMapper(connection))
		}
		
		return connection
	}
	
	override fun acceptRecovery(connectionId: Long): ServerConnection<I>? {
		val identity = identities[connectionId] ?: return null
		return connections[identity]?.takeIf { it.id == connectionId }
	}
	
	override fun releaseServerConnection(connection: ServerConnection<I>) {
		logger.trace { "Release connection ${connection.identity}" }
		
		_size.getAndDecrement()
		
		identities.remove(connection.id, connection.identity)
		connections.remove(connection.identity, connection)
		
		registry.remove(connection.identity)
	}
	
	fun stop() {
		if (_size.get() != 0) {
			logger.info { "Has $size connections, close them within $stopTimeoutSeconds seconds" }
			
			connections.values.forEach(Connection::close)
			
			if (waitIfLater(stopTimeoutSeconds * 1000) { _size.get() != 0 }) {
				logger.warn { "Not all connections closed, $size left, ignore them" }
				connections.clear()
			}
		}
	}
}