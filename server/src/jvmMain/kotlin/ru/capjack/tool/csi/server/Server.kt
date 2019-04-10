package ru.capjack.tool.csi.server

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.csi.server.internal.*
import ru.capjack.tool.io.putInt
import ru.capjack.tool.logging.debug
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.warn
import ru.capjack.tool.utils.Closeable
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.wait
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Server(
	private val executor: ScheduledExecutor,
	private val clientAuthorizer: ClientAuthorizer,
	private val clientAcceptor: ClientAcceptor,
	gateway: ConnectionGateway,
	private val connectionActivityTimeoutMilliseconds: Int
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
		logger.debug("Starting")
		gate = gateway.openGate(connections)
		logger.debug("Started")
	}
	
	fun stop(timeoutSeconds: Int) {
		synchronized(runningLock) {
			if (!running) {
				return
			}
			running = false
		}
		
		logger.debug("Stopping")
		
		connections.stop(timeoutSeconds)
		gate.close()
		
		logger.debug("Stopped")
	}
	
	private inner class Connections : ConnectionAcceptor, ConnectionReleaser, ReceptionConnectionProcessorHeir {
		val size = AtomicInteger()
		
		private val connections = ConcurrentHashMap.newKeySet<ConnectionDelegate>()
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
					val internalConnection =
						ConnectionDelegateImpl(connection, this, receptionConnectionProcessor, executor, connectionActivityTimeoutMilliseconds)
					connections.add(internalConnection)
					return internalConnection
				}
			}
			
			logger.trace { "Release connection ${connection.id} on shutdown" }
			connection.send(ProtocolFlag.CLOSE_SERVER_SHUTDOWN)
			connection.close()
			
			return FakeConnectionHandler
		}
		
		override fun releaseConnection(delegate: ConnectionDelegate) {
			logger.trace { "Release connection ${delegate.connectionId}" }
			size.getAndDecrement()
			connections.remove(delegate)
		}
		
		fun stop(timeoutSeconds: Int) {
			var s = size.get()
			if (s != 0) {
				var timeoutMs = timeoutSeconds * 1000L
				
				logger.debug { "Server has $s connections, shutdown after $timeoutMs milliseconds" }
				
				val message = ByteArray(5)
				message[0] = ProtocolFlag.SERVER_SHUTDOWN_TIMEOUT
				message.putInt(1, timeoutSeconds)
				connections.forEach { it.send(message) }
				
				Thread.sleep(timeoutMs)
				
				s = size.get()
				
				if (s != 0) {
					timeoutMs = s * 200L
					logger.debug { "Expect all connections to be closed within $timeoutMs milliseconds" }
					
					connections.forEach { it.close(ConnectionCloseReason.SERVER_SHUTDOWN) }
					
					if (wait(timeoutMs) { size.get() != 0 }) {
						s = size.get()
						timeoutMs = s * 100L
						logger.debug { "Exist $s opened connections, waiting additional $timeoutMs milliseconds" }
						
						if (wait(timeoutMs) { size.get() != 0 }) {
							logger.warn { "Failed to close $size connections, ignore them" }
							size.set(0)
							connections.clear()
						}
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
			
			val client = InternalClientImpl(clientId, delegate, executor, connectionActivityTimeoutMilliseconds)
			client.addDisconnectHandler(this)
			clients.compute(clientId, AuthorizationFinalizer(client, clientAcceptor))
			return client
		}
		
		override fun acceptRecovery(clientId: Long, sessionKey: Long): InternalClient? {
			return clients[clientId]?.takeIf { it.checkSessionKey(sessionKey) }
		}
		
		override fun handleClientDisconnect(client: Client) {
			logger.trace { "Release client ${client.id}" }
			
			size.getAndDecrement()
			
			clients.remove(client.id, client)
		}
	}
	
	private inner class Statistic : ServerStatistic {
		override val connections: Int
			get() = this@Server.connections.size.get()
		
		override val clients: Int
			get() = this@Server.clients.size.get()
		
	}
}
