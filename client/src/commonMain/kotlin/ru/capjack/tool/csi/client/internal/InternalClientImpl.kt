package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.ClientAcceptor
import ru.capjack.tool.csi.client.ClientDisconnectReason
import ru.capjack.tool.csi.client.ClientHandler
import ru.capjack.tool.csi.client.ConnectionAcceptor
import ru.capjack.tool.csi.client.ConnectionProducer
import ru.capjack.tool.csi.client.ConnectionRecoveryHandler
import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ConnectionHandler
import ru.capjack.tool.csi.common.OutgoingMessage
import ru.capjack.tool.csi.common.OutgoingMessageBuffer
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoIf
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.accessOrExecuteOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCapture

internal class InternalClientImpl(
	private val executor: ScheduledExecutor,
	private val connectionProducer: ConnectionProducer,
	private var delegate: ConnectionDelegate,
	private var sessionId: ByteArray,
	private val activityTimeoutMillis: Int
) : InternalClient, ConnectionProcessor {
	
	private val logger: Logger = ownLogger.wrap { "[${worker.alive.make("${delegate.connectionId}", "dead")}] $it" }
	private val worker = LivingWorker(executor, ::syncHandleError)
	
	private var processor: InternalClientProcessor = NothingInternalClientProcessor()
	
	private var lastReceivedMessageId = 0
	private var lastReceivedMessageIdSend = false
	private var lastReceivedMessageIdPackage = ByteArray(5).apply { set(0, ProtocolFlag.MESSAGE_RECEIVED) }
	
	private val outgoingMessages = OutgoingMessageBuffer()
	
	
	override fun accept(acceptor: ClientAcceptor) {
		logger.trace { "Accept" }
		syncSetProcessor(MessagingProcessor(acceptor.acceptSuccess(this)))
	}
	
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		logger.trace { "Try process input ${buffer.readableSize}B" }
		
		worker.withCapture {
			while (worker.alive && buffer.readable) {
				if (!processor.processInput(delegate, buffer)) {
					return false
				}
			}
			if (worker.alive) {
				syncSendLastReceivedMessageId()
			}
		}
		return true
	}
	
	override fun processLoss(delegate: ConnectionDelegate) {
		logger.trace { "Schedule lost" }
		
		worker.executeOnLive {
			if (this.delegate == delegate) {
				processor.processLoss()
			}
		}
	}
	
	override fun sendMessage(data: Byte) {
		logger.trace { "Schedule send message of 1B" }
		
		worker.accessOrExecuteOnLive {
			syncSendMessage(outgoingMessages.add(data))
		}
	}
	
	override fun sendMessage(data: ByteArray) {
		logger.trace { "Schedule send message of ${data.size}B" }
		
		worker.accessOrExecuteOnLive {
			syncSendMessage(outgoingMessages.add(data))
		}
	}
	
	override fun sendMessage(data: InputByteBuffer) {
		logger.trace { "Schedule send message of ${data.readableSize}B" }
		
		if (worker.alive) {
			if (worker.accessible) {
				syncSendMessage(outgoingMessages.add(data))
			}
			else {
				data.readToArray().also { bytes ->
					worker.execute {
						if (worker.alive) {
							syncSendMessage(outgoingMessages.add(bytes))
						}
					}
				}
			}
		}
	}
	
	override fun disconnect() {
		logger.trace { "Schedule disconnect" }
		
		worker.accessOrExecuteOnLive {
			val d = delegate
			syncSendLastReceivedMessageId()
			syncDisconnect(ClientDisconnectReason.CLOSE)
			d.close()
		}
	}
	
	private fun syncSetProcessor(processor: InternalClientProcessor) {
		logger.trace { "Use processor ${processor::class.simpleName}" }
		this.processor = processor
	}
	
	private fun syncSendMessage(message: OutgoingMessage) {
		logger.trace { "Send message id ${message.id}" }
		delegate.send(message.data)
	}
	
	private fun syncSendLastReceivedMessageId() {
		if (lastReceivedMessageIdSend) {
			logger.trace { "Send last received message $lastReceivedMessageId" }
			
			lastReceivedMessageIdSend = false
			lastReceivedMessageIdPackage.putInt(1, lastReceivedMessageId)
			delegate.send(lastReceivedMessageIdPackage)
		}
	}
	
	private fun syncDisconnect(reason: ClientDisconnectReason) {
		logger.trace { "Disconnect by $reason" }
		
		worker.die()
		
		processor.processDisconnect(reason)
		
		delegate = DummyConnectionDelegate()
		syncSetProcessor(NothingInternalClientProcessor())
		
		outgoingMessages.clear()
	}
	
	private fun syncHandleError(t: Throwable) {
		logger.error("Uncaught exception", t)
		delegate.close()
		syncDisconnect(ClientDisconnectReason.CLIENT_ERROR)
	}
	
	
	///
	
	private enum class MessagingInputState {
		MESSAGE_ID,
		MESSAGE_BODY,
		MESSAGE_RECEIVED
	}
	
	private inner class MessagingProcessor(private val handler: ClientHandler) : AbstractInputProcessor(), InternalClientProcessor {
		
		private var active = true
		private val activeChecker = executor.repeat(activityTimeoutMillis / 2, ::checkActivity)
		
		private var inputState = MessagingInputState.MESSAGE_ID
		private var inputMessageId = 0
		
		override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
			active = true
			return super.processInput(delegate, buffer)
		}
		
		override fun processInputFlag(delegate: ConnectionDelegate, flag: Byte): Boolean {
			return when (flag) {
				ProtocolFlag.MESSAGE          -> {
					inputState = MessagingInputState.MESSAGE_ID
					switchToBody()
					true
				}
				ProtocolFlag.MESSAGE_RECEIVED -> {
					inputState = MessagingInputState.MESSAGE_RECEIVED
					switchToBody()
					true
				}
				ProtocolFlag.PING             -> {
					true
				}
				else                          ->
					super.processInputFlag(delegate, flag)
			}
		}
		
		override fun processInputBody(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
			return when (inputState) {
				MessagingInputState.MESSAGE_ID       -> processInputMessageId(buffer)
				MessagingInputState.MESSAGE_BODY     -> processInputMessageBody(buffer.framedView)
				MessagingInputState.MESSAGE_RECEIVED -> processInputMessageReceived(buffer)
			}
		}
		
		private fun processInputMessageId(buffer: InputByteBuffer): Boolean {
			return buffer.isReadable(4).alsoIf {
				inputMessageId = buffer.readInt()
				inputState = MessagingInputState.MESSAGE_BODY
			}
		}
		
		private fun processInputMessageBody(frameBuffer: InputByteBuffer): Boolean {
			return frameBuffer.readable.alsoIf {
				logger.trace { "Receive message $inputMessageId" }
				lastReceivedMessageId = inputMessageId
				lastReceivedMessageIdSend = true
				switchToFlag()
				try {
					handler.handleMessage(frameBuffer)
				}
				catch (t: Throwable) {
					syncHandleError(t)
				}
			}
		}
		
		private fun processInputMessageReceived(buffer: InputByteBuffer): Boolean {
			return buffer.isReadable(4).alsoIf {
				val messageId = buffer.readInt()
				logger.trace { "Sent message $messageId delivered" }
				outgoingMessages.clearTo(messageId)
				switchToFlag()
			}
		}
		
		override fun processInputClose(reason: ConnectionCloseReason) {
			val disconnectReason = when (reason) {
				ConnectionCloseReason.CLOSE                    -> ClientDisconnectReason.CLOSE
				ConnectionCloseReason.SERVER_SHUTDOWN          -> ClientDisconnectReason.SERVER_SHUTDOWN
				ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED -> ClientDisconnectReason.CONNECTION_LOST
				ConnectionCloseReason.CONCURRENT               -> ClientDisconnectReason.CONCURRENT
				ConnectionCloseReason.PROTOCOL_BROKEN          -> ClientDisconnectReason.PROTOCOL_BROKEN
				ConnectionCloseReason.SERVER_ERROR             -> ClientDisconnectReason.SERVER_ERROR
				else                                           -> {
					logger.error("Unexpected close reason $reason")
					ClientDisconnectReason.PROTOCOL_BROKEN
				}
			}
			
			syncDisconnect(disconnectReason)
		}
		
		override fun processDisconnect(reason: ClientDisconnectReason) {
			activeChecker.cancel()
			handler.handleDisconnect(reason)
		}
		
		override fun processLoss() {
			activeChecker.cancel()
			delegate = DummyConnectionDelegate()
			
			val recoveryHandler = handler.handleConnectionLost()
			val recoveryProcessor = RecoveryProcessor(handler, recoveryHandler)
			
			syncSetProcessor(recoveryProcessor)
			connectionProducer.produceConnection(recoveryProcessor)
		}
		
		private fun checkActivity() {
			worker.execute {
				if (worker.alive) {
					if (active) {
						active = false
						delegate.send(ProtocolFlag.PING)
					}
					else {
						delegate.terminate()
						processLoss()
					}
				}
			}
		}
	}
	
	private inner class RecoveryProcessor(
		private val handler: ClientHandler,
		private val recoveryHandler: ConnectionRecoveryHandler
	) : AbstractInputProcessor(), InternalClientProcessor, ConnectionAcceptor {
		private val timeout = executor.schedule(activityTimeoutMillis, ::fail)
		
		override fun acceptSuccess(connection: Connection): ConnectionHandler {
			val d = ConnectionDelegateImpl(executor, connection, this@InternalClientImpl)
			
			worker.execute {
				if (worker.alive) {
					delegate = d
					delegate.send(ByteArray(1 + 16 + 4).also {
						it[0] = ProtocolFlag.RECOVERY
						sessionId.copyInto(it, 1)
						it.putInt(17, lastReceivedMessageId)
					})
				}
				else {
					d.close()
				}
			}
			
			return d
		}
		
		override fun acceptFail() {
			fail()
		}
		
		override fun processInputFlag(delegate: ConnectionDelegate, flag: Byte): Boolean {
			return if (flag == ProtocolFlag.RECOVERY) {
				switchToBody()
				true
			}
			else super.processInputFlag(delegate, flag)
		}
		
		override fun processInputBody(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
			return buffer.isReadable(4).alsoIf {
				timeout.cancel()
				
				sessionId = buffer.readToArray(16)
				
				val messageId = buffer.readInt()
				
				syncSetProcessor(MessagingProcessor(handler))
				
				outgoingMessages.clearTo(messageId)
				outgoingMessages.forEach(::syncSendMessage)
				
				recoveryHandler.handleConnectionRecovered()
				
				switchToFlag()
			}
		}
		
		override fun processInputClose(reason: ConnectionCloseReason) {
			val disconnectReason = when (reason) {
				ConnectionCloseReason.RECOVERY_REJECT          -> ClientDisconnectReason.CONNECTION_LOST
				ConnectionCloseReason.CLOSE                    -> ClientDisconnectReason.CLOSE
				ConnectionCloseReason.SERVER_SHUTDOWN          -> ClientDisconnectReason.SERVER_SHUTDOWN
				ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED -> ClientDisconnectReason.CONNECTION_LOST
				ConnectionCloseReason.CONCURRENT               -> ClientDisconnectReason.CONNECTION_LOST
				ConnectionCloseReason.PROTOCOL_BROKEN          -> ClientDisconnectReason.PROTOCOL_BROKEN
				ConnectionCloseReason.SERVER_ERROR             -> ClientDisconnectReason.SERVER_ERROR
				else                                           -> {
					logger.error("Unexpected close reason $reason")
					ClientDisconnectReason.PROTOCOL_BROKEN
				}
			}
			
			syncDisconnect(disconnectReason)
		}
		
		override fun processDisconnect(reason: ClientDisconnectReason) {
			handler.handleDisconnect(reason)
		}
		
		override fun processLoss() {
			syncDisconnect(ClientDisconnectReason.CONNECTION_LOST)
		}
		
		private fun fail() {
			timeout.cancel()
			worker.execute {
				if (worker.alive) {
					delegate.terminate()
					syncDisconnect(ClientDisconnectReason.CONNECTION_LOST)
				}
			}
		}
	}
	
}