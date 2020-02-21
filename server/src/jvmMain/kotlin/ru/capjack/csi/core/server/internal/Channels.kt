package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler
import ru.capjack.csi.core.common.DummyChannelHandler
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.server.ChannelAcceptor
import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.lang.waitIfImmediately
import ru.capjack.tool.logging.info
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.warn
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.tool.utils.concurrency.Sluice
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal class Channels<I : Any>(
	private val sluice: Sluice,
	private val byteBuffers: ObjectPool<ByteBuffer>,
	private val assistant: DelayableAssistant,
	serverVersion: Int,
	private val activityTimeoutSeconds: Int,
	private val shutdownTimeoutSeconds: Int,
	private val stopTimeoutSeconds: Int,
	authorizer: ConnectionAuthorizer<I>,
	authorizationAcceptor: ConnectionAuthorizationAcceptor<I>,
	recoveryAcceptor: ConnectionRecoveryAcceptor
) : ChannelAcceptor, ServerChannelReleaser {
	
	private val logger = ownLogger
	private val _size = AtomicInteger()
	private val channels = ConcurrentHashMap.newKeySet<ServerChannel>()
	private val receptionProcessor = ReceptionChannelProcessor(
		VersionValidatorChannelProcessor(serverVersion, AuthorizationChannelProcessor(authorizer, authorizationAcceptor)),
		RecoveryChannelProcessor(recoveryAcceptor)
	)
	
	val size: Int
		get() = _size.get()
	
	init {
		require(activityTimeoutSeconds > 0)
		require(shutdownTimeoutSeconds >= 0)
		require(stopTimeoutSeconds > 0)
	}
	
	override fun acceptChannel(channel: Channel): ChannelHandler {
		logger.trace { "Accept channel ${channel.id}" }
		
		sluice.pass {
			_size.getAndIncrement()
			val delegate = ServerChannelImpl(channel, receptionProcessor, byteBuffers, assistant, activityTimeoutSeconds, this)
			channels.add(delegate)
			return delegate
		}
		
		logger.trace { "Release channel ${channel.id} on shutdown" }
		channel.send(ProtocolMarker.SERVER_CLOSE_SHUTDOWN)
		channel.close()
		
		return DummyChannelHandler
	}
	
	override fun releaseServerChannel(channel: ServerChannel) {
		logger.trace { "Release channel ${channel.id}" }
		_size.getAndDecrement()
		
		channels.remove(channel)
	}
	
	fun stop() {
		if (_size.get() != 0) {
			
			if (shutdownTimeoutSeconds != 0) {
				logger.info { "Has $size channels, shutdown after $shutdownTimeoutSeconds seconds" }
				
				val message = ByteArray(5)
				message[0] = ProtocolMarker.SERVER_SHUTDOWN_TIMEOUT
				message.putInt(1, shutdownTimeoutSeconds)
				channels.forEach { it.send(message) }
				
				waitIfImmediately(shutdownTimeoutSeconds * 1000, 100) { _size.get() != 0 }
			}
			
			if (_size.get() != 0) {
				logger.info { "Has $size channels, close them within $stopTimeoutSeconds seconds" }
				
				channels.forEach {
					it.closeWithMarker(ProtocolMarker.SERVER_CLOSE_SHUTDOWN)
				}
				
				if (waitIfImmediately(stopTimeoutSeconds * 1000) { _size.get() != 0 }) {
					logger.warn { "Not all channels closed, $size left, ignore them" }
					channels.clear()
				}
			}
		}
	}
}