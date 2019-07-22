package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.OutgoingMessage
import ru.capjack.tool.csi.common.OutgoingMessageBuffer
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.csi.server.ClientAcceptor
import ru.capjack.tool.csi.server.ClientDisconnectHandler
import ru.capjack.tool.csi.server.ClientMessageReceiver
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoIf
import ru.capjack.tool.lang.make
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.warn
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.accessOrExecuteOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCapture
import kotlin.random.Random

internal class InternalClientImpl(
	override val id: Long,
	private var delegate: ConnectionDelegate,
	private val executor: ScheduledExecutor,
	private val activityTimeoutMillis: Int
) : InternalClient, ConnectionProcessor {
	
	private val logger: Logger = ownLogger.wrap { "[$id-${worker.alive.make("${delegate.connectionId}", "dead")}] $it" }
	private val worker = LivingWorker(executor, ::syncHandleError)
	
	@Volatile
	private var sessionKey = 0L
	
	private val disconnectHandlers = mutableListOf<ClientDisconnectHandler>()
	private var processor: InternalClientProcessor = AcceptationProcessor()
	
	private var lastReceivedMessageId = 0
	private var lastReceivedMessageIdSend = false
	private var lastReceivedMessageIdPackage = ByteArray(5).apply { set(0, ProtocolFlag.MESSAGE_RECEIVED) }
	
	private val outgoingMessages = OutgoingMessageBuffer()
	
	init {
		updateSessionKey()
	}
	
	override fun checkSessionKey(value: Long): Boolean {
		return sessionKey == value
	}
	
	override fun accept(acceptor: ClientAcceptor) {
		logger.trace { "Schedule accept" }
		
		worker.executeOnLive {
			logger.trace { "Accept" }
			updateSessionKey()
			syncSetProcessor(MessagingProcessor(acceptor.acceptClient(id, this)))
			delegate.send(ByteBuffer(1 + 8 + 8 + 4) {
				writeByte(ProtocolFlag.AUTHORIZATION)
				writeLong(id)
				writeLong(sessionKey)
				writeInt(activityTimeoutMillis)
			})
		}
	}
	
	override fun recovery(delegate: ConnectionDelegate, lastSentMessageId: Int) {
		logger.trace { "Schedule recovery" }
		
		worker.execute {
			if (worker.alive) {
				this.delegate.also {
					this.delegate = delegate
					it.close(ConnectionCloseReason.CONCURRENT)
				}
				
				logger.trace { "Recovery, resend messages after $lastSentMessageId" }
				
				processor.processRecovery()
				
				updateSessionKey()
				
				delegate.send(ByteBuffer(1 + 8 + 8 + 4) {
					writeByte(ProtocolFlag.RECOVERY)
					writeLong(id)
					writeLong(sessionKey)
					writeInt(lastReceivedMessageId)
				})
				
				outgoingMessages.clearTo(lastSentMessageId)
				outgoingMessages.forEach(::syncSendMessage)
				
				delegate.setProcessor(this)
			}
			else {
				delegate.close(ConnectionCloseReason.RECOVERY_REJECT)
			}
		}
	}
	
	override fun disconnectOfConcurrent() {
		logger.trace { "Schedule disconnect of concurrent" }
		worker.executeOnLive {
			delegate.close(ConnectionCloseReason.CONCURRENT)
		}
	}
	
	override fun disconnect() {
		logger.trace { "Schedule disconnect" }
		
		worker.accessOrExecuteOnLive {
			val d = delegate
			syncSendLastReceivedMessageId()
			syncDisconnect()
			
			d.close(ConnectionCloseReason.CLOSE)
		}
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
	
	override fun processClose(delegate: ConnectionDelegate, loss: Boolean) {
		logger.trace { "Process connection close${loss.make(" (lost)", "")}" }
		
		if (worker.alive) {
			if (!loss && worker.accessible && this.delegate == delegate) {
				syncDisconnect()
			}
			else {
				worker.execute {
					if (worker.alive) {
						if (this.delegate == delegate) {
							if (loss) {
								processor.processLoss()
							}
							else {
								syncDisconnect()
							}
						}
					}
				}
			}
		}
		
	}
	
	override fun sendMessage(data: Byte) {
		logger.trace { "Schedule send message 1B" }
		
		worker.accessOrExecuteOnLive {
			syncSendMessage(outgoingMessages.add(data))
		}
	}
	
	override fun sendMessage(data: ByteArray) {
		logger.trace { "Schedule send message ${data.size}B" }
		
		worker.accessOrExecuteOnLive {
			syncSendMessage(outgoingMessages.add(data))
		}
	}
	
	override fun sendMessage(data: InputByteBuffer) {
		logger.trace { "Schedule send message ${data.readableSize}B" }
		
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
	
	override fun addDisconnectHandler(handler: ClientDisconnectHandler) {
		worker.execute {
			if (worker.alive) {
				disconnectHandlers.add(handler)
			}
			else {
				handler.handleClientDisconnect(this)
			}
		}
	}
	
	private fun updateSessionKey() {
		sessionKey = Random.nextLong()
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
	
	private fun syncDisconnect() {
		logger.trace { "Disconnect" }
		
		worker.die()
		
		syncSetProcessor(NothingInternalClientProcessor)
		delegate = NothingConnectionDelegate
		
		disconnectHandlers.forEach { it.handleClientDisconnect(this) }
		disconnectHandlers.clear()
		outgoingMessages.clear()
	}
	
	private fun syncSetProcessor(processor: InternalClientProcessor) {
		logger.trace { "Use processor ${processor.javaClass.simpleName}" }
		this.processor = processor
	}
	
	private fun syncHandleError(t: Throwable) {
		logger.error("Uncaught exception", t)
		delegate.close(ConnectionCloseReason.SERVER_ERROR)
	}
	
	///
	
	private inner class AcceptationProcessor : InternalClientProcessor {
		override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
			delegate.close(ConnectionCloseReason.PROTOCOL_BROKEN)
			return false
		}
		
		override fun processLoss() {
			syncDisconnect()
		}
		
		override fun processRecovery() {
			throw UnsupportedOperationException()
		}
	}
	
	private enum class MessagingInputState {
		FLAG,
		MESSAGE_ID,
		MESSAGE_BODY,
		MESSAGE_RECEIVED
	}
	
	private inner class MessagingProcessor(private val receiver: ClientMessageReceiver) : InternalClientProcessor {
		
		private var inputState = MessagingInputState.FLAG
		private var inputMessageId = 0
		
		override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
			return when (inputState) {
				MessagingInputState.FLAG             -> processInputFlag(buffer.readByte())
				MessagingInputState.MESSAGE_ID       -> processInputMessageId(buffer)
				MessagingInputState.MESSAGE_BODY     -> processInputMessageBody(buffer.framedView)
				MessagingInputState.MESSAGE_RECEIVED -> processInputMessageReceived(buffer)
			}
		}
		
		override fun processLoss() {
			syncSetProcessor(RecoveryProcessor(receiver))
			delegate = DummyConnectionDelegate
		}
		
		override fun processRecovery() {
			inputState = MessagingInputState.FLAG
		}
		
		private fun processInputFlag(flag: Byte): Boolean {
			return when (flag) {
				ProtocolFlag.MESSAGE          -> {
					inputState = MessagingInputState.MESSAGE_ID
					true
				}
				ProtocolFlag.MESSAGE_RECEIVED -> {
					inputState = MessagingInputState.MESSAGE_RECEIVED
					true
				}
				ProtocolFlag.PING             -> {
					delegate.send(ProtocolFlag.PING)
					true
				}
				ProtocolFlag.CLOSE            -> {
					delegate.close()
					false
				}
				else                          -> {
					logger.warn { "Invalid messaging flag 0x${flag.toHexString()}" }
					delegate.close(ConnectionCloseReason.PROTOCOL_BROKEN)
					false
				}
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
				inputState = MessagingInputState.FLAG
				try {
					receiver.receiveMessage(frameBuffer)
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
				inputState = MessagingInputState.FLAG
			}
		}
	}
	
	private inner class RecoveryProcessor(private val receiver: ClientMessageReceiver) : InternalClientProcessor {
		
		private val timeout = executor.schedule(activityTimeoutMillis, ::disconnect)
		
		override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
			delegate.close(ConnectionCloseReason.PROTOCOL_BROKEN)
			return false
		}
		
		override fun processLoss() {
			throw UnsupportedOperationException()
		}
		
		override fun processRecovery() {
			timeout.cancel()
			syncSetProcessor(MessagingProcessor(receiver))
		}
	}
}

