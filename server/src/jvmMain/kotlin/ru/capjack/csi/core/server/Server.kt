package ru.capjack.csi.core.server

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionCloseReason
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.ProtocolFlag
import ru.capjack.csi.core.server.internal.*
import ru.capjack.tool.io.putInt
import ru.capjack.tool.lang.make
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.logging.info
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
	private val stopShutdownTimeoutMillis: Int,
	private val stopConnectionsTimeoutMillis: Int = 1000 * 10,
	private val stopClientsTimeoutMillis: Int = 1000 * 60 * 10,
	advancedStatistic: Boolean = false
) {
	val statistic: ServerStatistic
		get() = internalStatistic
	
	private val internalStatistic: InternalStatistic = if (advancedStatistic) AdvancedStatistic() else SimpleStatistic()
	
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
		require(stopConnectionsTimeoutMillis >= 0)
		require(stopClientsTimeoutMillis >= 0)
		
		logger.info("Starting")
		gate = gateway.open(connections)
		logger.info("Started")
	}
	
	fun stop() {
		
		synchronized(runningLock) {
			if (!running) {
				return
			}
			running = false
		}
		
		logger.info("Stopping")
		
		connections.stop()
		clients.stop()
		gate.close()
		
		logger.info("Stopped")
	}
	
	private inner class Connections : ConnectionAcceptor, ConnectionReleaser, ReceptionConnectionProcessorHeir {
		val size = AtomicInteger()
		private val delegates = ConcurrentHashMap.newKeySet<ConnectionDelegate>()
		private val receptionConnectionProcessor = ReceptionConnectionProcessor(this)
		private val authorizationConnectionProcessor = AuthorizationConnectionProcessor(clientAuthorizer, clients, internalStatistic.clients)
		private val recoveryConnectionProcessor = RecoveryConnectionProcessor(clients, internalStatistic.clients)
		
		override fun proceedAuthorization(): ConnectionProcessor {
			return authorizationConnectionProcessor
		}
		
		override fun proceedRecovery(): ConnectionProcessor {
			return recoveryConnectionProcessor
		}
		
		override fun acceptConnection(connection: Connection): ConnectionHandler {
			logger.trace { "Accept connection ${connection.id}" }
			internalStatistic.connections.addAccept()
			
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
			
			internalStatistic.connections.addRelease()
			
			return DummyConnectionHandler
		}
		
		override fun releaseConnection(delegate: ConnectionDelegate) {
			logger.trace { "Release connection ${delegate.connectionId}" }
			size.getAndDecrement()
			delegates.remove(delegate)
			
			internalStatistic.connections.addRelease()
		}
		
		fun stop() {
			if (size.get() != 0) {
				
				if (stopShutdownTimeoutMillis != 0) {
					logger.info { "Has $size connections, shutdown after ${stopShutdownTimeoutMillis / 1000.0} seconds" }
					
					val message = ByteArray(5)
					message[0] = ProtocolFlag.SERVER_SHUTDOWN_TIMEOUT
					message.putInt(1, stopShutdownTimeoutMillis)
					delegates.forEach { it.send(message) }
					
					waitIf(stopShutdownTimeoutMillis, if (stopShutdownTimeoutMillis > 1000) 1000 else 100) { size.get() != 0 }
				}
				
				if (size.get() != 0) {
					logger.info { "Has $size connections, close them within ${stopConnectionsTimeoutMillis / 1000.0} seconds" }
					
					delegates.forEach { it.close(ConnectionCloseReason.SERVER_SHUTDOWN) }
					
					if (waitIf(stopConnectionsTimeoutMillis) { size.get() != 0 }) {
						logger.warn { "Not all connections closed, $size left, ignore them" }
						size.set(0)
						delegates.clear()
					}
				}
			}
		}
	}
	
	private inner class Clients : AuthorizationConnectionProcessorHeir, ClientDisconnectHandler,
		RecoveryAcceptor {
		val size = AtomicInteger()
		
		private val clients = ConcurrentHashMap<Long, InternalClient>()
		
		override fun acceptAuthorization(delegate: ConnectionDelegate, clientId: Long): ConnectionProcessor {
			logger.trace { "Accept client $clientId" }
			
			size.getAndIncrement()
			
			val client = InternalClientImpl(clientId, delegate, executor, connectionActivityTimeoutMillis, internalStatistic.clients)
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
		
		fun stop() {
			if (size.get() != 0) {
				logger.info { "Has $size clients, disconnect them within ${stopClientsTimeoutMillis / 1000.0} seconds" }
				
				clients.values.forEach(InternalClient::disconnect)
				
				if (waitIf(stopClientsTimeoutMillis) { size.get() != 0 }) {
					logger.warn { "Not all clients disconnected, $size left, ignore them" }
					size.set(0)
					clients.clear()
				}
			}
		}
	}
	
	
	internal interface InternalStatistic : ServerStatistic {
		override val connections: Connections
		override val clients: Clients
		
		interface Connections : ServerStatistic.Connections {
			fun addAccept()
			fun addRelease()
		}
		
		interface Clients : ServerStatistic.Clients {
			fun addAuthorizationAccept()
			fun addAuthorizationReject()
			fun addRecoveryAccept()
			fun addRecoveryReject()
			fun addMessageInput()
			fun addMessageOutput()
			fun addMessageRecovery()
		}
	}
	
	internal inner class SimpleStatistic : InternalStatistic {
		override val connections = Connections()
		override val clients = Clients()
		
		override fun snap(): ServerStatistic = SnapshotServerStatistic(
			SnapshotServerStatistic.Connections(
				connections.total,
				connections.accept,
				connections.release
			),
			SnapshotServerStatistic.Clients(
				clients.total,
				clients.authorizationAccept,
				clients.authorizationReject,
				clients.recoveryAccept,
				clients.recoveryReject,
				clients.messageInput,
				clients.messageOutput,
				clients.messageRecovery
			)
		)
		
		override fun toString(): String = "connections: ${connections.total}, clients: ${clients.total}"
		
		inner class Connections : InternalStatistic.Connections {
			override val total: Int get() = this@Server.connections.size.get()
			override val accept: Int = 0
			override val release: Int = 0
			
			override fun addAccept() {}
			override fun addRelease() {}
			
			override fun toString(): String = total.toString()
		}
		
		inner class Clients : InternalStatistic.Clients {
			override val total: Int get() = this@Server.clients.size.get()
			override val authorizationAccept: Int = 0
			override val authorizationReject: Int = 0
			override val recoveryAccept: Int = 0
			override val recoveryReject: Int = 0
			override val messageInput: Int = 0
			override val messageOutput: Int = 0
			override val messageRecovery: Int = 0
			
			override fun addAuthorizationAccept() {}
			override fun addAuthorizationReject() {}
			override fun addRecoveryAccept() {}
			override fun addRecoveryReject() {}
			override fun addMessageInput() {}
			override fun addMessageOutput() {}
			override fun addMessageRecovery() {}
			
			override fun toString(): String = total.toString()
		}
	}
	
	internal inner class AdvancedStatistic : InternalStatistic {
		override val connections = Connections()
		override val clients = Clients()
		
		override fun snap(): ServerStatistic {
			return SnapshotServerStatistic(connections.snap(), clients.snap())
		}
		
		inner class Connections : InternalStatistic.Connections {
			private val _accept = AtomicInteger()
			private val _release = AtomicInteger()
			
			override val total: Int get() = this@Server.connections.size.get()
			override val accept: Int get() = _accept.get()
			override val release: Int get() = _release.get()
			
			override fun addAccept() {
				_accept.getAndIncrement()
			}
			
			override fun addRelease() {
				_release.getAndIncrement()
			}
			
			fun snap(): SnapshotServerStatistic.Connections {
				return SnapshotServerStatistic.Connections(
					total,
					_accept.getAndSet(0),
					_release.getAndSet(0)
				)
			}
		}
		
		inner class Clients : InternalStatistic.Clients {
			private val _authorizationAccept = AtomicInteger()
			private val _authorizationReject = AtomicInteger()
			private val _recoveryAccept = AtomicInteger()
			private val _recoveryReject = AtomicInteger()
			private val _messageReceive = AtomicInteger()
			private val _messageSend = AtomicInteger()
			private val _messageResend = AtomicInteger()
			
			override val total: Int get() = this@Server.clients.size.get()
			override val authorizationAccept: Int get() = _authorizationAccept.get()
			override val authorizationReject: Int get() = _authorizationReject.get()
			override val recoveryAccept: Int get() = _recoveryAccept.get()
			override val recoveryReject: Int get() = _recoveryReject.get()
			override val messageInput: Int get() = _messageReceive.get()
			override val messageOutput: Int get() = _messageSend.get()
			override val messageRecovery: Int get() = _messageResend.get()
			
			override fun addAuthorizationAccept() {
				_authorizationAccept.getAndIncrement()
			}
			
			override fun addAuthorizationReject() {
				_authorizationReject.getAndIncrement()
			}
			
			override fun addRecoveryAccept() {
				_recoveryAccept.getAndIncrement()
			}
			
			override fun addRecoveryReject() {
				_recoveryReject.getAndIncrement()
			}
			
			override fun addMessageInput() {
				_messageReceive.getAndIncrement()
			}
			
			override fun addMessageOutput() {
				_messageSend.getAndIncrement()
			}
			
			override fun addMessageRecovery() {
				_messageResend.getAndIncrement()
			}
			
			fun snap(): SnapshotServerStatistic.Clients {
				return SnapshotServerStatistic.Clients(
					total,
					_authorizationAccept.getAndSet(0),
					_authorizationReject.getAndSet(0),
					_recoveryAccept.getAndSet(0),
					_recoveryReject.getAndSet(0),
					_messageReceive.getAndSet(0),
					_messageSend.getAndSet(0),
					_messageResend.getAndSet(0)
				)
			}
		}
	}
}