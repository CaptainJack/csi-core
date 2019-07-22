package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ConnectionHandler
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.csi.server.internal.*
import ru.capjack.tool.io.putInt
import ru.capjack.tool.lang.make
import ru.capjack.tool.lang.waitIfImmediately
import ru.capjack.tool.logging.debug
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.warn
import ru.capjack.tool.utils.Closeable
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Server(
	private val executor: ScheduledExecutor,
	private val clientAuthorizer: ClientAuthorizer,
	private val clientAcceptor: ClientAcceptor,
	gateway: ConnectionGateway,
	private val connectionActivityTimeoutMillis: Int,
	private val stopShutdownTimeoutMillis: Int
) {
	val statistic: ServerStatistic = Statistic()
	
	private val logger = ownLogger
	private val clients = Clients()
	private val connections = Connections()
	private val gate: Closeable
	
	@Volatile
	private var running = true
	private var runningLock = Any()
	
	init {
		require(connectionActivityTimeoutMillis > 0)
		require(stopShutdownTimeoutMillis >= 0)
		
		logger.debug("Starting")
		gate = gateway.open(connections)
		logger.debug("Started")
	}
	
	fun stop() {
		
		synchronized(runningLock) {
			if (!running) {
				return
			}
			running = false
		}
		
		logger.debug("Stopping")
		
		connections.stop(stopShutdownTimeoutMillis)
		gate.close()
		
		logger.debug("Stopped")
	}
	
	private inner class Connections : ConnectionAcceptor, ConnectionReleaser, ReceptionConnectionProcessorHeir {
		val size = AtomicInteger()
		private val delegates = ConcurrentHashMap.newKeySet<ConnectionDelegate>()
		private val receptionConnectionProcessor = ReceptionConnectionProcessor(this)
		private val authorizationConnectionProcessor = AuthorizationConnectionProcessor(clientAuthorizer, clients)
		private val recoveryConnectionProcessor = RecoveryConnectionProcessor(clients)
		
		override fun acceptAuthorization(): ConnectionProcessor {
			return authorizationConnectionProcessor
		}
		
		override fun acceptRecovery(): ConnectionProcessor {
			return recoveryConnectionProcessor
		}
		
		override fun acceptConnection(connection: Connection): ConnectionHandler {
			logger.trace { "Accept connection ${connection.id}" }
			
			synchronized(runningLock) {
				if (running) {
					size.getAndIncrement()
					val delegate = ConnectionDelegateImpl(connection, this, receptionConnectionProcessor, executor, connectionActivityTimeoutMillis)
					delegates.add(delegate)
					return delegate
				}
			}
			
			logger.trace { "Release connection ${connection.id} on shutdown" }
			connection.send(ProtocolFlag.CLOSE_SERVER_SHUTDOWN)
			connection.close()
			
			return DummyConnectionHandler
		}
		
		override fun releaseConnection(delegate: ConnectionDelegate) {
			logger.trace { "Release connection ${delegate.connectionId}" }
			size.getAndDecrement()
			delegates.remove(delegate)
		}
		
		fun stop(timeoutMilliseconds: Int) {
			var s = size.get()
			if (s != 0) {
				
				var timeoutMs: Long
				
				if (timeoutMilliseconds != 0) {
					timeoutMs = timeoutMilliseconds.toLong()
					
					logger.debug { "Server has $s connections, shutdown after $timeoutMs milliseconds" }
					
					val message = ByteArray(5)
					message[0] = ProtocolFlag.SERVER_SHUTDOWN_TIMEOUT
					message.putInt(1, timeoutMilliseconds)
					delegates.forEach { it.send(message) }
					
					Thread.sleep(timeoutMs)
				}
				
				s = size.get()
				
				if (s != 0) {
					timeoutMs = (s * 200L).coerceAtMost(10L * 60 * 1000)
					logger.debug { "Expect all connections to be closed within $timeoutMs milliseconds" }
					
					delegates.forEach { it.close(ConnectionCloseReason.SERVER_SHUTDOWN) }
					
					if (waitIfImmediately(timeoutMs) { size.get() != 0 }) {
						logger.warn { "Failed to close $size connections, ignore them" }
						size.set(0)
						delegates.clear()
					}
				}
			}
		}
	}
	
	private inner class Clients : AuthorizationConnectionProcessorHeir, ClientDisconnectHandler, RecoveryAcceptor {
		val size = AtomicInteger()
		
		private val clients = ConcurrentHashMap<Long, InternalClient>()
		
		override fun acceptClient(delegate: ConnectionDelegate, clientId: Long): ConnectionProcessor {
			logger.trace { "Accept client $clientId" }
			
			size.getAndIncrement()
			
			val client = InternalClientImpl(clientId, delegate, executor, connectionActivityTimeoutMillis)
			client.addDisconnectHandler(this)
			clients.compute(clientId, AuthorizationFinalizer(client, clientAcceptor))
			return client
		}
		
		override fun acceptRecovery(clientId: Long, sessionKey: Long): InternalClient? {
			return clients[clientId]?.takeIf {
				it.checkSessionKey(sessionKey)
			}
		}
		
		override fun handleClientDisconnect(client: Client) {
			size.getAndDecrement()
			
			val removed = clients.remove(client.id, client)
			
			logger.trace { "${removed.make("Release", "Forget")} client ${client.id}" }
		}
	}
	
	private inner class Statistic : ServerStatistic {
		override val connections: Int
			get() = this@Server.connections.size.get()
		
		override val clients: Int
			get() = this@Server.clients.size.get()
		
	}
}
